package lt_web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import lt_web.utils.FileStorage;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/image")
public class DownloadImageController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String fileName = req.getParameter("fname");
        if (fileName != null && !fileName.isBlank()) {
            Path file = FileStorage.resolve(fileName);
            if (file != null && Files.isRegularFile(file)) {
                String lower = fileName.toLowerCase();
                if (lower.endsWith(".png")) {
                    resp.setContentType("image/png");
                } else if (lower.endsWith(".svg")) {
                    resp.setContentType("image/svg+xml");
                } else if (lower.endsWith(".gif")) {
                    resp.setContentType("image/gif");
                } else if (lower.endsWith(".webp")) {
                    resp.setContentType("image/webp");
                } else {
                    resp.setContentType("image/jpeg");
                }
                try (FileInputStream in = new FileInputStream(file.toFile());
                     OutputStream out = resp.getOutputStream()) {
                    in.transferTo(out);
                }
                return;
            }
        }

        resp.setContentType("image/svg+xml;charset=UTF-8");
        String color = "#1976d2";
        String label = "Danh mục";
        if ("default-women.svg".equalsIgnoreCase(fileName)) {
            color = "#ff5722";
            label = "Nữ";
        } else if ("default-men.svg".equalsIgnoreCase(fileName)) {
            color = "#1565c0";
            label = "Nam";
        }
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 150" width="200" height="150">
                  <rect width="200" height="150" fill="#ffffff"/>
                  <path d="M65,25 L85,25 C90,35 110,35 115,25 L135,25 L160,50 L142,68 L130,55 L130,130 L70,130 L70,55 L58,68 L40,50 Z" fill="%s"/>
                  <text x="100" y="95" font-family="Arial, sans-serif" font-size="13" fill="#ffffff" text-anchor="middle" font-weight="bold">%s</text>
                </svg>
                """.formatted(color, label);
        resp.getOutputStream().write(svg.getBytes(StandardCharsets.UTF_8));
    }
}
