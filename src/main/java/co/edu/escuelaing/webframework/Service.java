package co.edu.escuelaing.webframework;

/**
 * Functional interface for a lambda-based route handler.
 * Equivalent to: (req, resp) -> "some result"
 */
@FunctionalInterface
public interface Service {
    String handle(Request request, Response response);
}