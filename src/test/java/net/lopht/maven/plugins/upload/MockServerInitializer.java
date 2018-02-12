package net.lopht.maven.plugins.upload;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpStatusCode;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MockServerInitializer implements org.mockserver.initialize.ExpectationInitializer {

    @Override
    public void initializeExpectations(MockServerClient client) {
        // Single file PUT
        client.when(
            request()
                .withMethod("PUT")
                .withPath("/it-put-file/file1.txt")
        )
        .respond(
            response()
                .withStatusCode(HttpStatusCode.NO_CONTENT_204.code())
        );
        // Single file POST
        client.when(
            request()
                .withMethod("POST")
                .withPath("/it-post-file/file1.txt")
        )
        .respond(
            response()
                .withStatusCode(HttpStatusCode.CREATED_201.code())
        );
        // Multiple files PUT
        client.when(
            request()
                .withMethod("PUT")
                .withPath("/it-put-files/.*")
        )
        .respond(
            response()
                .withStatusCode(HttpStatusCode.NO_CONTENT_204.code())
        );
        // Multiple files POST
        client.when(
            request()
                .withMethod("POST")
                .withPath("/it-post-files/.*")
        )
        .respond(
            response()
                .withStatusCode(HttpStatusCode.CREATED_201.code())
        );
    }
    
}