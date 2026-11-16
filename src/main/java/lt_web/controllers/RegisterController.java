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
import java.sql.Date;
import lt_web.entity.User;
import lt_web.services.OtpService;
import lt_web.services.UserService;
import lt_web.services.impl.OtpServiceImpl;
import lt_web.services.impl.UserServiceImpl;
import lt_web.utils.Constant;
import lt_web.utils.ValidationUtil;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/register")
public class RegisterController extends HttpServlet {
    private final UserService userService = new UserServiceImpl();
    private final OtpService otpService = new OtpServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(Constant.SESSION_USERNAME) != null) {
            resp.sendRedirect(req.getContextPath() + "/waiting");
            return;
        }
        req.getRequestDispatcher(Constant.Path.REGISTER).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        req.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String repassword = req.getParameter("repassword");
        String email = req.getParameter("email");
        String fullname = req.getParameter("fullname");
        String phone = req.getParameter("phone");

        preserveFormFields(req, username, fullname, email, phone);

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showError(req, resp, "Tài khoản và mật khẩu không được để trống!");
            return;
        }

        if (email == null || email.isBlank()) {
            showError(req, resp, "Email không được để trống để nhận mã kích hoạt OTP!");
            return;
        }

        if (repassword != null && !repassword.isBlank() && !password.equals(repassword)) {
            showError(req, resp, "Mật khẩu nhập lại không khớp!");
            return;
        }

        User candidate = new User(email.trim(), username.trim(), fullname, password,
                null, 5, phone, new Date(System.currentTimeMillis()));
        String validationMessage = ValidationUtil.firstMessage(candidate);
        if (validationMessage != null) {
            showError(req, resp, validationMessage);
            return;
        }

        if (userService.checkExistEmail(email.trim())) {
            showError(req, resp, "Email đã tồn tại!");
            return;
        }

        if (userService.checkExistUsername(username.trim())) {
            showError(req, resp, "Tài khoản đã tồn tại!");
            return;
        }

        if (userService.checkExistPhone(phone)) {
            showError(req, resp, "Số điện thoại đã tồn tại!");
            return;
        }

        boolean isSuccess = userService.register(username.trim(), password, email.trim(), fullname, phone);
        if (isSuccess) {
            try {
                otpService.generateAndSendActivationOtp(email.trim(), fullname);
            } catch (Exception e) {
                log("Không thể gửi email OTP kích hoạt", e);
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("activationEmail", email.trim());
            String encodedEmail = URLEncoder.encode(email.trim(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/verify-otp?email=" + encodedEmail + "&type=activation");
        } else {
            showError(req, resp, "Lỗi hệ thống khi tạo tài khoản. Vui lòng thử lại!");
        }
    }

    private void preserveFormFields(HttpServletRequest req, String username, String fullname, String email, String phone) {
        req.setAttribute("username", username);
        req.setAttribute("fullname", fullname);
        req.setAttribute("email", email);
        req.setAttribute("phone", phone);
    }

    private void showError(HttpServletRequest req, HttpServletResponse resp, String alert)
            throws ServletException, IOException {
        req.setAttribute("alert", alert);
        req.getRequestDispatcher(Constant.Path.REGISTER).forward(req, resp);
    }
}

