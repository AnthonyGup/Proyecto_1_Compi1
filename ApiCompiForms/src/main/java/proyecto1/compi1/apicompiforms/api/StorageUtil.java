package proyecto1.compi1.apicompiforms.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public final class StorageUtil {

    public static final class PkmFileInfo {
        public String fileName;
        public String createdAt;
        public long sizeBytes;
    }

    private static final Path UPLOAD_DIR = Paths.get(
        System.getProperty("user.home"),
        "ApiCompiForms",
        "uploads"
    );

    private StorageUtil() {
    }

    public static Path getUploadDir() throws IOException {
        Files.createDirectories(UPLOAD_DIR);
        return UPLOAD_DIR;
    }

    public static String sanitizeFileName(String rawName) {
        String fileName = rawName == null ? "" : rawName;
        int slash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (slash >= 0) {
            fileName = fileName.substring(slash + 1);
        }
        fileName = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
        if (fileName.isBlank()) {
            fileName = "form_" + System.currentTimeMillis() + ".pkm";
        }
        if (!fileName.toLowerCase().endsWith(".pkm")) {
            fileName = fileName + ".pkm";
        }
        return fileName;
    }

    public static Path resolveTarget(String fileName) throws IOException {
        Path base = getUploadDir().toRealPath();
        Path target = base.resolve(fileName).normalize();
        if (!target.startsWith(base)) {
            throw new IOException("Nombre de archivo invalido");
        }
        return target;
    }

    public static void saveUpload(InputStream inputStream, String fileName) throws IOException {
        Path target = resolveTarget(fileName);
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public static List<PkmFileInfo> listPkmFiles() throws IOException {
        Path base = getUploadDir();
        List<PkmFileInfo> files = new ArrayList<>();

        try (var stream = Files.list(base)) {
            stream
                .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(".pkm"))
                .forEach(path -> {
                    try {
                        String fileName = path.getFileName().toString();
                        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

                        PkmFileInfo info = new PkmFileInfo();
                        info.fileName = fileName;
                        info.createdAt = attrs.creationTime().toString();
                        info.sizeBytes = attrs.size();
                        files.add(info);
                    } catch (IOException ignored) {
                        // Skip unreadable files
                    }
                });
        }

        return files;
    }
}
