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
    /**
     * The path to the folder to scan for files to upload.
     *
     * @since 0.0.1
     */
    @Parameter (property="upload.basedir", defaultValue="${project.basedir}")
    private File basedir;

    /**
     * List of Ant file patterns to include from <i>basedir</i>.
     *
     * @since 0.0.1
     */
    @Parameter (property="upload.includes")
    private String[] includes;

    /**
     * List of Ant file patterns to exclude from <i>basedir</i>.
     *
     * @since 0.0.1
     */
    @Parameter (property="upload.excludes")
    private String[] excludes;

    /**
     * The server path to the folder where the files will be uploaded, ie path/to/folder.
     * Will be appended to the <i>repositoryUrl</i> parameter.
     *
     * @since 0.1.0
     */
    @Parameter (property="upload.repositoryBasePath")
    private String repositoryBasepath;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if (skip) {
            getLog().info("Skipping execution per configuration");
            return;
        }

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
