package lt_web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lt_web.entity.User;
import lt_web.services.OtpService;
import lt_web.services.UserService;
import lt_web.services.impl.OtpServiceImpl;
import lt_web.services.impl.UserServiceImpl;
import lt_web.utils.Constant;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/forgot-password")
public class ForgotPasswordController extends HttpServlet {
    private final UserService userService = new UserServiceImpl();
    private final OtpService otpService = new OtpServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(Constant.SESSION_ACCOUNT) != null) {
            resp.sendRedirect(req.getContextPath() + "/waiting");
            return;
        }
        req.getRequestDispatcher(Constant.Path.FORGOT_PASSWORD).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String email = req.getParameter("email");
        if (email == null || email.isBlank()) {
            req.setAttribute("alert", "Vui lòng nhập email đã đăng ký.");
            req.getRequestDispatcher(Constant.Path.FORGOT_PASSWORD).forward(req, resp);
            return;
        }

        String normalizedEmail = email.trim();
        req.setAttribute("email", normalizedEmail);

        User user = userService.findByEmail(normalizedEmail);
        if (user == null) {
            req.setAttribute("alert", "Email không tồn tại trong hệ thống.");
            req.getRequestDispatcher(Constant.Path.FORGOT_PASSWORD).forward(req, resp);
            return;
        }

        try {
            otpService.generateAndSendForgotPasswordOtp(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            log("Lỗi khi gửi email OTP quên mật khẩu", e);
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("forgotEmail", user.getEmail());
        String encodedEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/verify-otp?email=" + encodedEmail + "&type=forgot");
    }
}
