package co.edu.escuelaing.webframework;

/**
 * A registered dynamic route: a path paired with the lambda that handles it.
 */
public class Route {

    private final String path;
    private final Service service;

    public Route(String path, Service service) {
        this.path = path;
        this.service = service;
    }

    public String getPath() {
        return path;
    }

    public Service getService() {
        return service;
    }
}
