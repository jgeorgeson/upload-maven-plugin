package net.lopht.maven.plugins.upload;

import java.io.File;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Uploads file to remote repository.
 *
 */
@Mojo (name="upload-file", defaultPhase=LifecyclePhase.DEPLOY)
public class FileUploadMojo
    extends AbstractUploadMojo
{
    /*
     * The path to the file to be uploaded.
     * @since 0.0.1
     */
    @Parameter (property="upload.file")
    private File file;

    /*
     * The server path where the file will be uploaded, ie path/to/file.ext.
     * Will be appended to the <i>repositoryUrl</i> parameter.
     * @since 0.0.1
     */
    @Parameter (property="upload.repositoryPath")
    private String repositoryPath;

    /*
     * If true, fail build when file is missing.
     * @since 0.0.1
     */
    @Parameter (property="upload.ignoreMissing", defaultValue="false")
    private boolean ignoreMissingFile;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( ignoreMissingFile && !file.exists() )
        {
            getLog().info( "File does not exist, ignoring " + file.getAbsolutePath() );
            return;
        }

        ArtifactRepository repository = getArtifactRepository();

        CloseableHttpClient client = getHttpClient( repository );

        String url = getTargetUrl( repository );

        getLog().info( "Upload target url: " + url );

        uploadFile( client, file, url );
    }

    private String getTargetUrl( ArtifactRepository repository )
    {
        StringBuilder sb = new StringBuilder( repository.getUrl() );

        if ( !repository.getUrl().endsWith( "/" ) && !repositoryPath.startsWith( "/" ) )
        {
            sb.append( "/" );
        }

        sb.append( repositoryPath );

        return sb.toString();
    }

}
