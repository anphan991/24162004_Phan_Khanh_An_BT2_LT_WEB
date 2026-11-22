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
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "ogg", "mov", "mkv");

    private FileStorage() {
    }

    public static String saveImage(Part part) throws IOException {
        return savePart(part, IMAGE_EXTENSIONS, "Định dạng ảnh không được hỗ trợ.");
    }

    public static String saveVideo(Part part) throws IOException {
        return savePart(part, VIDEO_EXTENSIONS, "Định dạng video không được hỗ trợ (hỗ trợ MP4, WEBM, OGG, MOV, MKV).");
    }

    public static boolean isAllowedImage(String fileName) {
        return fileName != null && IMAGE_EXTENSIONS.contains(extensionOf(fileName));
    }

    public static boolean isAllowedVideo(String fileName) {
        return fileName != null && VIDEO_EXTENSIONS.contains(extensionOf(fileName));
    }

    public static String getFileName(Part part) {
        if (part == null) {
            return null;
        }
        String submitted = part.getSubmittedFileName();
        if (submitted != null && !submitted.isBlank()) {
            return Path.of(submitted).getFileName().toString();
        }
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String content : contentDisposition.split(";")) {
                String trimmed = content.trim();
                if (trimmed.startsWith("filename")) {
                    int eq = trimmed.indexOf('=');
                    if (eq >= 0) {
                        String name = trimmed.substring(eq + 1).trim().replace("\"", "");
                        if (!name.isBlank()) {
                            return Path.of(name).getFileName().toString();
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String savePart(Part part, Set<String> allowedExtensions, String invalidTypeMessage)
            throws IOException {
        String originalName = getFileName(part);
        if (part == null || part.getSize() == 0 || originalName == null || originalName.isBlank()) {
            return null;
        }
        String extension = extensionOf(originalName);
        if (!allowedExtensions.contains(extension)) {
            throw new IOException(invalidTypeMessage);
        }

        Path directory = Constant.uploadDirectory();
        Files.createDirectories(directory);
        String storedName = UUID.randomUUID() + "." + extension;
        Path target = directory.resolve(storedName).normalize();
        if (!target.startsWith(directory)) {
            throw new IOException("Đường dẫn lưu tệp không hợp lệ.");
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

    public static void deleteQuietly(String fileName) {
        if (fileName == null || fileName.isBlank()
                || fileName.startsWith("default-")
                || fileName.startsWith("http://")
                || fileName.startsWith("https://")) {
            return;
        }
        try {
            delete(fileName);
        } catch (IOException ignored) {
            // Không làm gián đoạn luồng chính nếu file tạm không xóa được.
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
