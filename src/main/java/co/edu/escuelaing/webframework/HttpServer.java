package co.edu.escuelaing.webframework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Sequential HTTP server. Accepts one connection at a time, parses the
 * request line, and delegates to the Router for dynamic routes or to the
 * StaticFileService as a fallback. No concurrency mechanism is used.
 */
public class HttpServer {

    private final Router router;
    private final StaticFileService staticFileService;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public HttpServer(Router router, StaticFileService staticFileService) {
        this.router = router;
        this.staticFileService = staticFileService;
    }

    public void start(int port) throws IOException {
        running = true;
        try (ServerSocket socket = new ServerSocket(port)) {
            this.serverSocket = socket;
            System.out.println("Server listening on port " + port + "...");

            while (running) {
                Socket clientSocket = null;
                try {
                    clientSocket = socket.accept();
                    handleClient(clientSocket);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error handling client: " + e.getMessage());
                    }
                    // If !running, this IOException is expected: it comes
                    // from closing the ServerSocket during shutdown.
                } finally {
                    closeQuietly(clientSocket);
                }
            }
        }
        System.out.println("Server stopped gracefully.");
    }

    /**
     * Marks the server as no longer running and closes the listening
     * socket so the blocking accept() call unblocks and the loop exits.
     * This is called AFTER the current response has already been sent
     * (see handleClient), so the current request finishes normally.
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
            // Nothing more we can do; the loop will exit on the next check.
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
        OutputStream out = clientSocket.getOutputStream();

        String requestLine = in.readLine();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            // Drain remaining headers.
        }

        if (requestLine == null || requestLine.isBlank()) {
            return;
        }
        System.out.println("Request line: " + requestLine);

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            sendError(out, 400, "Bad Request");
            return;
        }

        String method = parts[0];
        String rawPath = parts[1];

        if (!"GET".equalsIgnoreCase(method)) {
            sendError(out, 405, "Method Not Allowed");
            return;
        }

        String pathOnly = rawPath.contains("?")
                ? rawPath.substring(0, rawPath.indexOf('?'))
                : rawPath;
        String decodedPath = URLDecoder.decode(pathOnly, StandardCharsets.UTF_8);
        Map<String, String> queryParams = parseQuery(rawPath);

        // 1. Try a registered dynamic route first.
        Route route = router.resolve(decodedPath);
        if (route != null) {
            Request request = new Request(decodedPath, queryParams);
            Response response = new Response();
            String body;
            try {
                body = route.getService().handle(request, response);
            } catch (Exception e) {
                System.err.println("Error executing route " + decodedPath + ": " + e.getMessage());
                sendError(out, 500, "Internal Server Error");
                return;
            }
            byte[] bodyBytes = (body == null ? "" : body).getBytes(StandardCharsets.UTF_8);
            sendResponse(out, 200, "OK", response.getContentType(), bodyBytes);
            return;
        }

        // 2. Fall back to static resources.
        String staticPath = decodedPath.equals("/") ? "/index.html" : decodedPath;
        if (staticFileService.canServe(staticPath)) {
            try {
                if (!staticFileService.exists(staticPath)) {
                    sendError(out, 404, "Not Found");
                    return;
                }
                byte[] body = staticFileService.readBytes(staticPath);
                String contentType = staticFileService.getContentType(staticPath);
                sendResponse(out, 200, "OK", contentType, body);
            } catch (SecurityException e) {
                System.err.println("Rejected unsafe path: " + staticPath);
                sendError(out, 404, "Not Found");
            }
            return;
        }

        // 3. Neither a dynamic route nor a servable static file.
        sendError(out, 404, "Not Found");
    }

    private Map<String, String> parseQuery(String rawPath) {
        Map<String, String> result = new HashMap<>();
        if (!rawPath.contains("?")) {
            return result;
        }
        String query = rawPath.substring(rawPath.indexOf('?') + 1);
        for (String pair : query.split("&")) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }

    private void sendResponse(OutputStream out, int status, String statusText,
                              String contentType, byte[] body) throws IOException {
        String headers = "HTTP/1.1 " + status + " " + statusText + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";
        out.write(headers.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    private void sendError(OutputStream out, int status, String statusText) throws IOException {
        String body = status + " " + statusText;
        sendResponse(out, status, statusText, "text/plain; charset=UTF-8",
                body.getBytes(StandardCharsets.UTF_8));
    }

    private void closeQuietly(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // Nothing more we can do.
            }
        }
    }
}
