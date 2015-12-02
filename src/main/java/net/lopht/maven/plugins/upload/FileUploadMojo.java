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
    @Parameter (property="upload.file")
    private File file;

    @Parameter (property="upload.repositoryPath")
    private String repositoryPath;

    @Parameter (defaultValue="false")
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
