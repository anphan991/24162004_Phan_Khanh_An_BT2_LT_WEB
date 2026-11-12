package lt_web.controllers.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lt_web.exceptions.DataAccessException;
import lt_web.entity.User;
import lt_web.services.UserService;
import lt_web.services.impl.UserServiceImpl;
import lt_web.utils.AdminAccess;
import lt_web.utils.Constant;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/admin/user/delete")
public class UserDeleteController extends HttpServlet {
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!AdminAccess.isAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list");
            return;
        }
        int id = parseId(req.getParameter("id"));
        HttpSession session = req.getSession(false);
        User current = session == null ? null : (User) session.getAttribute(Constant.SESSION_ACCOUNT);
        if (current != null && current.getId() == id) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list?userError=self#users-section");
            return;
        }

        try {
            if (id > 0) {
                userService.delete(id);
            }
            resp.sendRedirect(req.getContextPath() + "/admin/category/list?userDeleted=1#users-section");
        } catch (DataAccessException e) {
            log("Không thể xóa tài khoản", e);
            resp.sendRedirect(req.getContextPath() + "/admin/category/list?userError=delete#users-section");
        }
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
