package lt_web.controllers;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lt_web.entity.User;
import lt_web.services.RememberMeService;
import lt_web.utils.Constant;

@WebFilter(urlPatterns = "/*")
public class RememberMeFilter implements Filter {
    private final RememberMeService rememberMeService = new RememberMeService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (!isStaticResource(req)) {
            HttpSession session = req.getSession(false);
            boolean authenticated = session != null
                    && session.getAttribute(Constant.SESSION_ACCOUNT) instanceof User;
            if (!authenticated) {
                User account = rememberMeService.authenticate(getRememberToken(req));
                if (account != null) {
                    session = req.getSession(true);
                    session.setAttribute(Constant.SESSION_ACCOUNT, account);
                    session.setAttribute(Constant.SESSION_USERNAME, account.getUserName());
                }
            }
        }
        chain.doFilter(request, response);
    }

    private String getRememberToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (Constant.COOKIE_REMEMBER.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean isStaticResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".svg")
                || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg")
                || uri.endsWith(".gif") || uri.endsWith(".webp") || uri.endsWith(".ico");
    }
}
