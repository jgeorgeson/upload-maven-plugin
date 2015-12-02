package net.lopht.maven.plugins.upload;

import java.io.File;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Uploads multiple files to remote repository.
 * 
 */
@Mojo (name="upload-files", defaultPhase=LifecyclePhase.DEPLOY)
public class FilesUploadMojo
    extends AbstractUploadMojo
{
    @Parameter (property="upload.basedir", defaultValue="${project.basedir}")
    private File basedir;

    @Parameter (property="upload.includes")
    private String[] includes;

    @Parameter (property="upload.excludes")
    private String[] excludes;

    @Parameter (property="upload.repositoryBasePath")
    private String repositoryBasepath;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        ArtifactRepository repository = getArtifactRepository();

        CloseableHttpClient client = getHttpClient( repository );

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( basedir );
        scanner.addDefaultExcludes();
        scanner.setIncludes( includes );
        scanner.setExcludes( excludes );
        scanner.scan();

        String baseUrl = repository.getUrl();
        if ( !baseUrl.endsWith( "/" ) )
        {
            baseUrl = baseUrl + "/";
        }
        if ( repositoryBasepath != null )
        {
            baseUrl = baseUrl + repositoryBasepath;
        }
        if ( !baseUrl.endsWith( "/" ) )
        {
            baseUrl = baseUrl + "/";
        }

        for ( String relPath : scanner.getIncludedFiles() )
        {
            String path = relPath.replace( '\\', '/' );
            uploadFile( client, new File( basedir, path ), baseUrl + path );
        }
    }

}
