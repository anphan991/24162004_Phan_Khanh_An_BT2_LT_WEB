package lt_web.controllers;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lt_web.entity.User;
import lt_web.services.UserService;
import lt_web.services.impl.UserServiceImpl;
import lt_web.utils.Constant;

@WebFilter(urlPatterns = {"/admin/*", "/admin", "/manager/*", "/home"})
public class AuthorizationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User account = (session != null) ? (User) session.getAttribute(Constant.SESSION_ACCOUNT) : null;

        // Khôi phục account từ session hiện tại nếu có.
        if (account == null) {
            String username = (session != null) ? (String) session.getAttribute(Constant.SESSION_USERNAME) : null;
            if (username != null && !username.isBlank()) {
                UserService userService = new UserServiceImpl();
                account = userService.get(username);
                if (account != null) {
                    session = req.getSession(true);
                    session.setAttribute(Constant.SESSION_ACCOUNT, account);
                    session.setAttribute(Constant.SESSION_USERNAME, account.getUserName());
                }
            }
        }

        // Chưa đăng nhập -> bắt buộc chuyển về trang /login
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }

        String path = req.getServletPath();
        boolean isAdminRoute = path.startsWith("/admin") || path.startsWith("/manager");

        // Nếu vào trang quản trị (/admin/* hoặc /manager/*) nhưng không phải Admin (1) hoặc Manager (2)
        if (isAdminRoute && account.getRoleid() != 1 && account.getRoleid() != 2) {
            resp.sendRedirect(req.getContextPath() + "/home?forbidden=1");
            return;
        }

        // Chỉ Admin được thay đổi tài khoản; Manager chỉ xem dashboard.
        if (path.startsWith("/admin/user") && account.getRoleid() != 1) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list?forbidden=1#users-section");
            return;
        }

        chain.doFilter(request, response);
    }

}
