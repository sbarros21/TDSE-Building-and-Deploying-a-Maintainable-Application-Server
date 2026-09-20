package co.edu.escuelaing.webframework;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Serves static resources (HTML, CSS, JS, images) as bytes, from a
 * configurable base directory. Rejects any path that attempts to escape
 * that directory.
 */
public class StaticFileService {

    private static final Map<String, String> CONTENT_TYPES = new HashMap<>();
    static {
        CONTENT_TYPES.put("html", "text/html; charset=UTF-8");
        CONTENT_TYPES.put("css", "text/css; charset=UTF-8");
        CONTENT_TYPES.put("js", "text/javascript; charset=UTF-8");
        CONTENT_TYPES.put("png", "image/png");
        CONTENT_TYPES.put("jpg", "image/jpeg");
        CONTENT_TYPES.put("jpeg", "image/jpeg");
    }

    private Path baseDir;

    /**
     * Configures the static resources directory. Accepts either a
     * classpath-style path (e.g. "/webroot", resolved against
     * src/main/resources at dev time) or an absolute/relative filesystem
     * path (used at deployment time via STATIC_FILES_PATH).
     */
    public void configure(String location) {
        String cleaned = location.startsWith("/") ? location.substring(1) : location;

        Path devPath = Paths.get("src/main/resources", cleaned).toAbsolutePath().normalize();
        Path deployPath = Paths.get(cleaned).toAbsolutePath().normalize();

        if (Files.isDirectory(devPath)) {
            this.baseDir = devPath;
        } else if (Files.isDirectory(deployPath)) {
            this.baseDir = deployPath;
        } else {
            // Default to the dev path even if missing yet, so error
            // messages are meaningful instead of silently null.
            this.baseDir = devPath;
        }
        System.out.println("Static files served from: " + baseDir);
    }

    public boolean canServe(String path) {
        String extension = getExtension(path);
        return CONTENT_TYPES.containsKey(extension);
    }

    public byte[] readBytes(String path) throws IOException {
        Path resolved = resolveSafe(path);
        return Files.readAllBytes(resolved);
    }

    public boolean exists(String path) {
        try {
            Path resolved = resolveSafe(path);
            return Files.exists(resolved) && !Files.isDirectory(resolved);
        } catch (SecurityException e) {
            return false;
        }
    }

    public String getContentType(String path) {
        return CONTENT_TYPES.get(getExtension(path));
    }

    private Path resolveSafe(String path) {
        String relative = path.startsWith("/") ? path.substring(1) : path;
        Path resolved = baseDir.resolve(relative).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new SecurityException("Path traversal attempt: " + path);
        }
        return resolved;
    }

    private String getExtension(String path) {
        int dot = path.lastIndexOf('.');
        return dot == -1 ? "" : path.substring(dot + 1).toLowerCase();
    }
}
