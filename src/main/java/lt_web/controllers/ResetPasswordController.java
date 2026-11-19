package lt_web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import lt_web.entity.User;
import lt_web.services.OtpService;
import lt_web.services.UserService;
import lt_web.services.impl.OtpServiceImpl;
import lt_web.services.impl.UserServiceImpl;
import lt_web.utils.Constant;
import lt_web.utils.ValidationUtil;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/reset-password")
public class ResetPasswordController extends HttpServlet {
    private final UserService userService = new UserServiceImpl();
    private final OtpService otpService = new OtpServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = resolveEmail(req);
        if (email == null || email.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }

        req.setAttribute("email", email);
        req.setAttribute("isOtpVerified", isOtpPreVerified(req, email));
        req.getRequestDispatcher(Constant.Path.RESET_PASSWORD).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String email = resolveEmail(req);
        String otp = req.getParameter("otp");
        String password = req.getParameter("password");
        String repassword = req.getParameter("repassword");

        if (email == null || email.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
            return;
        }

        req.setAttribute("email", email);
        boolean preVerified = isOtpPreVerified(req, email);
        req.setAttribute("isOtpVerified", preVerified);

        Map<String, String> errors = new LinkedHashMap<>();
        if (!preVerified) {
            if (!ValidationUtil.isValidOtp(otp)) {
                errors.put("otp", "Vui lòng nhập mã OTP gồm đúng 6 chữ số.");
            }
        }
        if (password == null || password.isBlank()) {
            errors.put("password", "Mật khẩu mới không được để trống.");
        } else if (!ValidationUtil.isValidPassword(password)) {
            errors.put("password", "Mật khẩu mới phải từ 6 đến 100 ký tự.");
        }
        if (repassword == null || repassword.isBlank()) {
            errors.put("repassword", "Vui lòng nhập lại mật khẩu mới.");
        } else if (password != null && !password.equals(repassword)) {
            errors.put("repassword", "Mật khẩu xác nhận không khớp.");
        }
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            showError(req, resp, errors.values().iterator().next());
            return;
        }

        if (!preVerified) {
            boolean verified = otpService.verifyForgotPasswordOtp(email, otp.trim());
            if (!verified) {
                errors.put("otp", "Mã OTP không chính xác hoặc đã hết hạn.");
                req.setAttribute("errors", errors);
                showError(req, resp, "Mã OTP không chính xác hoặc đã hết hạn.");
                return;
            }
        }

        User user = userService.findByEmail(email);
        if (user == null) {
            showError(req, resp, "Không tìm thấy tài khoản người dùng.");
            return;
        }

        boolean success = userService.resetPassword(email, password);
        if (success) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.removeAttribute("verifiedResetEmail");
                session.removeAttribute("forgotEmail");
            }
            resp.sendRedirect(req.getContextPath() + "/login?resetSuccess=1");
        } else {
            showError(req, resp, "Không thể cập nhật mật khẩu. Vui lòng thử lại.");
        }
    }

    private boolean isOtpPreVerified(HttpServletRequest req, String email) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object verified = session.getAttribute("verifiedResetEmail");
            return verified instanceof String s && s.equalsIgnoreCase(email);
        }
        return false;
    }

    private String resolveEmail(HttpServletRequest req) {
        String email = req.getParameter("email");
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object sessionEmail = session.getAttribute("verifiedResetEmail");
            if (sessionEmail == null) {
                sessionEmail = session.getAttribute("forgotEmail");
            }
            if (sessionEmail instanceof String s && !s.isBlank()) {
                return s.trim();
            }
        }
        return null;
    }

    private void showError(HttpServletRequest req, HttpServletResponse resp, String alert)
            throws ServletException, IOException {
        req.setAttribute("alert", alert);
        req.getRequestDispatcher(Constant.Path.RESET_PASSWORD).forward(req, resp);
    }
}
