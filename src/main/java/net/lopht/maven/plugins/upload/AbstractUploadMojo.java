package net.lopht.maven.plugins.upload;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.repository.Proxy;
import org.apache.maven.repository.RepositorySystem;

public abstract class AbstractUploadMojo
    extends AbstractMojo
{
    @Component
    protected RepositorySystem repositorySystem;

    @Component
    protected ArtifactRepositoryLayout repositoryLayout;

    /**
     * @since 0.0.1
     */
    @Parameter (property="session")
    protected MavenSession session;

    /**
     * The server Id in settings.xml with credentials to use.
     *
     * @since 0.0.1
     */
    @Parameter (property="upload.serverId")
    protected String serverId;

    /**
     * The base URL of the server, ie http://server.example.com/.
     *
     * @since 0.0.1
     */
    @Parameter (property="upload.repositoryUrl")
    protected String repositoryUrl;

    /**
     * Set to true to skip execution.
     *
     * @since 0.4.0
     */
    @Parameter (property="upload.skip", defaultValue="false")
    protected boolean skip;

    /**
     * Set to true if the server requires credentials in the initial request.
     *
     * @since 0.3.0
     */
    @Parameter (property="upload.preemptiveAuth", defaultValue="false")
    protected boolean preemptiveAuth;

    /**
     * Custom HTTP headers to add to each request.
     *
     * @since 0.2.0
     */
    @Parameter
    protected Map<String,String> headers;

    /**
     * Use POST instead of PUT
     *
     * @since 0.5.0
     */
    @Parameter (property="upload.post", defaultValue="false")
    protected boolean usePOST;

    protected CloseableHttpClient getHttpClient( ArtifactRepository repository )
        throws MojoExecutionException
    {
        HttpClientBuilder clientBuilder = HttpClients.custom();
        CredentialsProvider credsProvider = null;

        Authentication authentication = repository.getAuthentication();
        if ( authentication != null )
        {
            getLog().debug("Found credentials: username="
                + authentication.getUsername()
                + " password="
                + authentication.getPassword());
            credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(AuthScope.ANY),
                    new UsernamePasswordCredentials(authentication.getUsername(),authentication.getPassword()));
            clientBuilder.setDefaultCredentialsProvider(credsProvider);
        }

        Proxy proxy = repository.getProxy();
        if ( proxy != null )
        {
            // NonProxyHosts is handled by ArtifactRepository, the Proxy will not be present here for NonProxyHosts
            if ( proxy.getProtocol() == null || proxy.getProtocol().equalsIgnoreCase(Proxy.PROXY_HTTP) )
            {
                getLog().debug("Found Proxy configuration: " + proxy.getHost() + ":" + proxy.getPort());
                HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort());
                clientBuilder.setProxy(proxyHost);

                if ( proxy.getUserName() != null )
                {
                    getLog().debug("Found proxy credentials: username=" + proxy.getUserName());
                    // Add CredentialsProvider if one was not added for target host Authentication
                    if ( credsProvider == null )
                    {
                        credsProvider = new BasicCredentialsProvider();
                        clientBuilder.setDefaultCredentialsProvider(credsProvider);
                    }
                    credsProvider.setCredentials(
                            new AuthScope(proxyHost), 
                            new UsernamePasswordCredentials(proxy.getUserName(), proxy.getPassword()));
                    clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
                }
            } else {
                throw new MojoExecutionException("Proxy protocol " + proxy.getProtocol() + " is not supported yet");
            }
        }
        return clientBuilder.build();
    }

    protected ArtifactRepository getArtifactRepository()
    {
        ArtifactRepositoryPolicy policy = new ArtifactRepositoryPolicy();
        ArtifactRepository repository =
            repositorySystem.createArtifactRepository( serverId, repositoryUrl, repositoryLayout, policy, policy );

        List<ArtifactRepository> repositories = new ArrayList<ArtifactRepository>();
        repositories.add( repository );

        // repositorySystem.injectMirror( artifactRepositories, session.getRequest().getMirrors() );

        repositorySystem.injectProxy( repositories, session.getRequest().getProxies() );

        repositorySystem.injectAuthentication( repositories, session.getRequest().getServers() );

        repository = repositories.get( 0 );
        return repository;
    }

    protected void uploadFile( CloseableHttpClient client, File file, String targetUrl )
        throws MojoExecutionException
    {
        getLog().info( "Uploading " + file.getAbsolutePath() + " to " + targetUrl );
        HttpEntityEnclosingRequestBase request;
        if (usePOST) {
            request = new HttpPost(targetUrl);
        } else {
            request = new HttpPut(targetUrl);
        }
        CloseableHttpResponse response = null;
        try
        {
            // Set Content type
            ContentType contentType = null;
            if ( file.getName().endsWith( ".xml" ) )
            {
                contentType = ContentType.APPLICATION_XML;
            }

            // Add the file to the PUT request
            request.setEntity( new FileEntity( file , contentType ) );

            if (null != headers) {
                for (Map.Entry<String,String> entry : headers.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            }

            if (preemptiveAuth) {
                // Auth target host
                URL aURL = new URL(targetUrl);
                HttpHost target = new HttpHost (aURL.getHost(), aURL.getPort(), aURL.getProtocol());
                // Create AuthCache instance
                AuthCache authCache = new BasicAuthCache();
                // Generate BASIC scheme object and add it to the local auth cache
                BasicScheme basicAuth = new BasicScheme();
                authCache.put(target, basicAuth);
                // Add AuthCache to the execution context
                HttpClientContext localContext = HttpClientContext.create();
                localContext.setAuthCache(authCache);
                // Execute request with pre-emptive authentication
                response = client.execute(request,localContext);
            } else {
                // Execute request, server will prompt for authentication if needed
                response = client.execute(request);
            }

            int status = response.getStatusLine().getStatusCode();
            if ( status < 200 || status > 299 )
            {
                String message = "Could not upload file: " + response.getStatusLine().toString();
                getLog().error( message );
                String responseBody = EntityUtils.toString(response.getEntity());
                if ( responseBody != null )
                {
                    getLog().info( responseBody );
                }
                throw new MojoExecutionException( message );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not upload file: ", e );
        }
        finally
        {
           	request.releaseConnection();
        }
    }

}
