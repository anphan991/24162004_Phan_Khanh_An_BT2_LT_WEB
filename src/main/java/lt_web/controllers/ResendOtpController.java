package lt_web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lt_web.entity.OtpToken;
import lt_web.entity.User;
import lt_web.services.OtpService;
import lt_web.services.UserService;
import lt_web.services.impl.OtpServiceImpl;
import lt_web.services.impl.UserServiceImpl;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/resend-otp")
public class ResendOtpController extends HttpServlet {
    private final OtpService otpService = new OtpServiceImpl();
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processResend(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processResend(req, resp);
    }

    private void processResend(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String email = req.getParameter("email");
        String type = req.getParameter("type");
        if (email == null || email.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String normalizedEmail = email.trim();
        String normalizedType = ("forgot".equalsIgnoreCase(type) || OtpToken.TYPE_FORGOT_PASSWORD.equalsIgnoreCase(type))
                ? OtpToken.TYPE_FORGOT_PASSWORD : OtpToken.TYPE_ACTIVATION;

        User user = userService.findByEmail(normalizedEmail);
        String fullName = user != null ? user.getFullName() : null;

        try {
            otpService.resendOtp(normalizedEmail, normalizedType, fullName);
        } catch (Exception e) {
            log("Lỗi khi gửi lại OTP", e);
        }

        String encodedEmail = URLEncoder.encode(normalizedEmail, StandardCharsets.UTF_8);
        String targetType = OtpToken.TYPE_FORGOT_PASSWORD.equals(normalizedType) ? "forgot" : "activation";
        resp.sendRedirect(req.getContextPath() + "/verify-otp?email=" + encodedEmail + "&type=" + targetType + "&resent=1");
    }
}
