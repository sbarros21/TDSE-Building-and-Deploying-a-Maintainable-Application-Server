package co.edu.escuelaing.webframework;

import java.util.Map;

/**
 * Represents an incoming HTTP request: the resolved path and its
 * query-string parameters. Application lambdas only see this abstraction,
 * never sockets or raw request lines.
 */
public class Request {

    private final String path;
    private final Map<String, String> queryParams;

    public Request(String path, Map<String, String> queryParams) {
        this.path = path;
        this.queryParams = queryParams;
    }

    public String getPath() {
        return path;
    }

    /**
     * Returns the value of a query-string parameter, or null if it is
     * missing. A missing parameter never throws.
     */
    public String getValue(String name) {
        return queryParams.get(name);
    }
}