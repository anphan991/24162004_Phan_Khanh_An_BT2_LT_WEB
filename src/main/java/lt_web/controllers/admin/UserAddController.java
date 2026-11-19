package lt_web.controllers.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import lt_web.exceptions.DataAccessException;
import lt_web.entity.User;
import lt_web.services.UserService;
import lt_web.services.impl.UserServiceImpl;
import lt_web.utils.AdminAccess;
import lt_web.utils.Constant;
import lt_web.utils.ValidationUtil;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/admin/user/add")
public class UserAddController extends HttpServlet {
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!AdminAccess.isAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list");
            return;
        }
        User user = new User();
        user.setRoleid(5);
        showForm(req, resp, user, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!AdminAccess.isAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list");
            return;
        }
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String username = trim(req.getParameter("username"));
        String password = req.getParameter("password");
        User user = readUser(req, username);

        Map<String, String> errors = new LinkedHashMap<>();
        if (username.isBlank()) {
            errors.put("username", "Tên đăng nhập không được để trống.");
        } else if (!ValidationUtil.isValidUsername(username)) {
            errors.put("username", "Tên đăng nhập từ 3-50 ký tự, chỉ gồm chữ, số, '.', '_' hoặc '-'.");
        }
        if (user.getFullName() == null || trim(req.getParameter("fullname")).isEmpty()) {
            errors.put("fullname", "Họ và tên không được để trống.");
        } else if (trim(req.getParameter("fullname")).length() < 2 || trim(req.getParameter("fullname")).length() > 100) {
            errors.put("fullname", "Họ và tên phải từ 2 đến 100 ký tự.");
        }
        if (user.getEmail() != null && !ValidationUtil.isValidEmail(user.getEmail())) {
            errors.put("email", "Địa chỉ email không đúng định dạng.");
        }
        if (user.getPhone() != null && !ValidationUtil.isValidPhone(user.getPhone())) {
            errors.put("phone", "Số điện thoại phải gồm 9-15 chữ số hợp lệ.");
        }
        if (password == null || password.isBlank()) {
            errors.put("password", "Mật khẩu không được để trống.");
        } else if (!ValidationUtil.isValidPassword(password)) {
            errors.put("password", "Mật khẩu phải từ 6 đến 100 ký tự.");
        }

        if (!errors.containsKey("username") && userService.checkExistUsername(username)) {
            errors.put("username", "Tên đăng nhập đã tồn tại.");
        }
        if (!errors.containsKey("email") && user.getEmail() != null && userService.checkExistEmail(user.getEmail())) {
            errors.put("email", "Email đã tồn tại.");
        }
        if (!errors.containsKey("phone") && user.getPhone() != null && userService.checkExistPhone(user.getPhone())) {
            errors.put("phone", "Số điện thoại đã tồn tại.");
        }
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            showForm(req, resp, user, errors.values().iterator().next());
            return;
        }

        try {
            userService.create(user, password);
            resp.sendRedirect(req.getContextPath() + "/admin/category/list#users-section");
        } catch (DataAccessException e) {
            log("Không thể thêm tài khoản", e);
            showForm(req, resp, user, "Không thể lưu tài khoản. Vui lòng thử lại.");
        }
    }

    private User readUser(HttpServletRequest req, String username) {
        User user = new User();
        user.setUserName(username);
        user.setFullName(trim(req.getParameter("fullname")));
        user.setEmail(trimOrNull(req.getParameter("email")));
        user.setPhone(trimOrNull(req.getParameter("phone")));
        user.setRoleid(parseRole(req.getParameter("roleid")));
        return user;
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, User user, String alert)
            throws ServletException, IOException {
        req.setAttribute("user", user);
        req.setAttribute("formMode", "add");
        if (alert != null) {
            req.setAttribute("alert", alert);
        }
        req.getRequestDispatcher(Constant.Path.ADMIN_USER_FORM).forward(req, resp);
    }

    private int parseRole(String value) {
        try {
            int role = Integer.parseInt(value);
            return role == 1 || role == 2 || role == 5 ? role : 5;
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimOrNull(String value) {
        String trimmed = trim(value);
        return trimmed.isBlank() ? null : trimmed;
    }
}
