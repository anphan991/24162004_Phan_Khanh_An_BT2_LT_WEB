package lt_web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lt_web.entity.User;
import lt_web.services.RememberMeService;
import lt_web.services.UserService;
import lt_web.services.impl.UserServiceImpl;
import lt_web.utils.Constant;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    private final RememberMeService rememberMeService = new RememberMeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(Constant.SESSION_ACCOUNT) != null) {
            resp.sendRedirect(req.getContextPath() + "/waiting");
            return;
        }

        User rememberedUser = authenticateRememberedUser(req, resp);
        if (rememberedUser != null) {
            establishSession(req, rememberedUser);
            resp.sendRedirect(req.getContextPath() + "/waiting");
            return;
        }

        // Sau khi logout, hiển thị form để người dùng có thể đổi account.
        if ("1".equals(req.getParameter("signedOut"))) {
            req.getRequestDispatcher(Constant.Path.LOGIN).forward(req, resp);
            return;
        }

        req.getRequestDispatcher(Constant.Path.LOGIN).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        req.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String remember = req.getParameter("remember");
        boolean isRememberMe = "on".equals(remember);
        req.setAttribute("rememberedUsername", username == null ? "" : username.trim());
        req.setAttribute("remembered", isRememberMe);

        String alertMsg = "";
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            alertMsg = "Tài khoản hoặc mật khẩu không được rỗng";
            req.setAttribute("alert", alertMsg);
            req.getRequestDispatcher(Constant.Path.LOGIN).forward(req, resp);
            return;
        }

        UserService service = new UserServiceImpl();
        User user = service.login(username.trim(), password);
        if (user != null) {
            if (user.getStatus() != null && user.getStatus() == 0) {
                req.setAttribute("alert", "Tài khoản chưa được kích hoạt. Vui lòng kích hoạt qua mã OTP đã gửi tới email.");
                req.setAttribute("unactivatedEmail", user.getEmail());
                req.getRequestDispatcher(Constant.Path.LOGIN).forward(req, resp);
                return;
            }
            establishSession(req, user);
            if (isRememberMe) {
                saveRememberMe(req, resp, user);
            } else {
                revokeRememberMe(req, resp);
            }
            resp.sendRedirect(req.getContextPath() + "/waiting");
        } else {
            alertMsg = "Tài khoản hoặc mật khẩu không đúng";
            req.setAttribute("alert", alertMsg);
            req.getRequestDispatcher(Constant.Path.LOGIN).forward(req, resp);
        }
    }

    private void saveRememberMe(HttpServletRequest request, HttpServletResponse response, User user) {
        String rawToken = rememberMeService.issue(user);
        Cookie cookie = new Cookie(Constant.COOKIE_REMEMBER, rawToken);
        cookie.setMaxAge(RememberMeService.COOKIE_MAX_AGE_SECONDS);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private void revokeRememberMe(HttpServletRequest request, HttpServletResponse response) {
        rememberMeService.revoke(getCookieValue(request, Constant.COOKIE_REMEMBER));
        clearCookie(response, Constant.COOKIE_REMEMBER);
        clearCookie(response, Constant.LEGACY_COOKIE_REMEMBER);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private User authenticateRememberedUser(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = getCookieValue(request, Constant.COOKIE_REMEMBER);
        if (rawToken == null) {
            return null;
        }
        User user = rememberMeService.authenticate(rawToken);
        if (user == null) {
            clearCookie(response, Constant.COOKIE_REMEMBER);
        }
        return user;
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())
                    && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void establishSession(HttpServletRequest request, User user) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(Constant.SESSION_ACCOUNT, user);
        session.setAttribute(Constant.SESSION_USERNAME, user.getUserName());
    }
}
