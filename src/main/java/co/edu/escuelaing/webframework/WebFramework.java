package co.edu.escuelaing.webframework;

import java.io.IOException;

/**
 * Public static facade of the framework. Application code interacts only
 * with these methods (get, staticfiles, start, stop) and never touches
 * sockets, the router, or the static-file service directly.
 */
public class WebFramework {

    private static final Router router = new Router();
    private static final StaticFileService staticFileService = new StaticFileService();
    private static final HttpServer server = new HttpServer(router, staticFileService);

    private WebFramework() {
        // Static facade; not instantiable.
    }

    public static void staticfiles(String location) {
        staticFileService.configure(location);
    }

    public static void get(String path, Service service) {
        router.addRoute(path, service);
    }

    public static void start() throws IOException {
        int port = resolvePort();
        server.start(port);
    }

    public static void start(int port) throws IOException {
        server.start(port);
    }

    public static void stop() {
        server.stop();
    }

    private static int resolvePort() {
        String portValue = System.getenv("PORT");
        if (portValue == null || portValue.isBlank()) {
            return 8080;
        }
        try {
            return Integer.parseInt(portValue.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid PORT value, using default 8080");
            return 8080;
        }
    }
}
