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

        String username = trim(req.getParameter("username"));
        String password = req.getParameter("password");
        String repassword = req.getParameter("repassword");
        String email = trim(req.getParameter("email"));
        String fullname = trim(req.getParameter("fullname"));
        String phone = trim(req.getParameter("phone"));

        preserveFormFields(req, username, fullname, email, phone);

        Map<String, String> errors = new LinkedHashMap<>();
        if (username.isEmpty()) {
            errors.put("username", "Tên đăng nhập không được để trống.");
        } else if (!ValidationUtil.isValidUsername(username)) {
            errors.put("username", "Tên đăng nhập từ 3-50 ký tự, chỉ gồm chữ, số, '.', '_' hoặc '-'.");
        }

        if (fullname.isEmpty()) {
            errors.put("fullname", "Họ và tên không được để trống.");
        } else if (fullname.length() < 2 || fullname.length() > 100) {
            errors.put("fullname", "Họ và tên phải từ 2 đến 100 ký tự.");
        }

        if (email.isEmpty()) {
            errors.put("email", "Email không được để trống để nhận mã kích hoạt OTP.");
        } else if (!ValidationUtil.isValidEmail(email)) {
            errors.put("email", "Địa chỉ email không đúng định dạng.");
        }

        if (phone.isEmpty()) {
            errors.put("phone", "Số điện thoại không được để trống.");
        } else if (!ValidationUtil.isValidPhone(phone)) {
            errors.put("phone", "Số điện thoại phải gồm 9-15 chữ số hợp lệ.");
        }

        if (password == null || password.isBlank()) {
            errors.put("password", "Mật khẩu không được để trống.");
        } else if (!ValidationUtil.isValidPassword(password)) {
            errors.put("password", "Mật khẩu phải từ 6 đến 100 ký tự.");
        }

        if (repassword == null || repassword.isBlank()) {
            errors.put("repassword", "Vui lòng nhập lại mật khẩu.");
        } else if (password != null && !password.equals(repassword)) {
            errors.put("repassword", "Mật khẩu nhập lại không khớp.");
        }

        User candidate = new User(email, username, fullname, password == null ? "" : password,
                null, 5, phone, new Date(System.currentTimeMillis()));
        for (Map.Entry<String, String> entry : ValidationUtil.fieldErrors(candidate).entrySet()) {
            String key = switch (entry.getKey()) {
                case "userName" -> "username";
                case "fullName" -> "fullname";
                case "passWord" -> "password";
                default -> entry.getKey();
            };
            errors.putIfAbsent(key, entry.getValue());
        }

        if (!errors.containsKey("email") && userService.checkExistEmail(email)) {
            errors.put("email", "Email đã tồn tại trong hệ thống.");
        }
        if (!errors.containsKey("username") && userService.checkExistUsername(username)) {
            errors.put("username", "Tên đăng nhập đã tồn tại.");
        }
        if (!errors.containsKey("phone") && userService.checkExistPhone(phone)) {
            errors.put("phone", "Số điện thoại đã tồn tại.");
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            showError(req, resp, errors.values().iterator().next());
            return;
        }

        boolean isSuccess = userService.register(username, password, email, fullname, phone);
        if (isSuccess) {
            try {
                otpService.generateAndSendActivationOtp(email, fullname);
            } catch (Exception e) {
                log("Không thể gửi email OTP kích hoạt", e);
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("activationEmail", email);
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/verify-otp?email=" + encodedEmail + "&type=activation");
        } else {
            showError(req, resp, "Lỗi hệ thống khi tạo tài khoản. Vui lòng thử lại!");
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
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

