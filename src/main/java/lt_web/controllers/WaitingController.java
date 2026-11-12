package lt_web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lt_web.entity.User;
import lt_web.services.UserService;
import lt_web.services.impl.UserServiceImpl;
import lt_web.utils.Constant;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/waiting")
public class WaitingController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User u = (User) session.getAttribute(Constant.SESSION_ACCOUNT);
            if (u == null && session.getAttribute(Constant.SESSION_USERNAME) != null) {
                UserService service = new UserServiceImpl();
                u = service.get(String.valueOf(session.getAttribute(Constant.SESSION_USERNAME)));
                if (u != null) {
                    session.setAttribute(Constant.SESSION_ACCOUNT, u);
                }
            }
            if (u != null) {
                req.setAttribute("username", u.getUserName());
                if (u.getRoleid() == 1) {
                    resp.sendRedirect(req.getContextPath() + "/admin/home");
                } else if (u.getRoleid() == 2) {
                    resp.sendRedirect(req.getContextPath() + "/manager/home");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/home");
                }
                return;
            }
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
