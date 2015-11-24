package org.sonatype.maven.plugins.upload;

import java.io.File;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Uploads file to remote repository.
 * 
 * @goal upload-file
 */
public class FileUploadMojo
    extends AbstractUploadMojo
{
    /** @parameter property="upload.file" */
    private File file;

    /** @parameter property="upload.repositoryPath" */
    private String repositoryPath;

    /** @parameter default-value="false" */
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
