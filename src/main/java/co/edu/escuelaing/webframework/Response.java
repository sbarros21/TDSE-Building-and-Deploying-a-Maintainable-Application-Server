package co.edu.escuelaing.webframework;

/**
 * Represents an outgoing HTTP response. Currently a thin abstraction:
 * lambdas return a String body directly, and this class lets us extend
 * response behavior (status, headers) later without changing the
 * framework's public API.
 */
public class Response {

    private String contentType = "text/plain; charset=UTF-8";

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
