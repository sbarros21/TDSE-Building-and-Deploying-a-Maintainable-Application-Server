package co.edu.escuelaing.webframework;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps request paths to registered lambda handlers. Adding a new route
 * never requires touching the HTTP server's connection loop.
 */
public class Router {

    private final Map<String, Route> routes = new HashMap<>();

    public void addRoute(String path, Service service) {
        routes.put(path, new Route(path, service));
    }

    public Route resolve(String path) {
        return routes.get(path);
    }

    public boolean hasRoute(String path) {
        return routes.containsKey(path);
    }
}
