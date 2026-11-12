package lt_web.utils;

import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class FileStorage {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "svg", "webp");

    private FileStorage() {
    }

    public static String saveImage(Part part) throws IOException {
        if (part == null || part.getSize() == 0 || part.getSubmittedFileName() == null) {
            return null;
        }
        String originalName = Path.of(part.getSubmittedFileName()).getFileName().toString();
        String extension = extensionOf(originalName);
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            throw new IOException("Định dạng ảnh không được hỗ trợ.");
        }

        Path directory = Constant.uploadDirectory();
        Files.createDirectories(directory);
        String storedName = UUID.randomUUID() + "." + extension;
        Path target = directory.resolve(storedName).normalize();
        if (!target.startsWith(directory)) {
            throw new IOException("Đường dẫn lưu ảnh không hợp lệ.");
        }
        try (var input = part.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedName;
    }

    public static Path resolve(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        Path directory = Constant.uploadDirectory();
        Path resolved = directory.resolve(fileName).normalize();
        return resolved.startsWith(directory) ? resolved : null;
    }

    public static void delete(String fileName) throws IOException {
        Path file = resolve(fileName);
        if (file != null) {
            Files.deleteIfExists(file);
        }
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
