package lt_web.controllers.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lt_web.exceptions.DataAccessException;
import lt_web.entity.Category;
import lt_web.services.CategoryService;
import lt_web.services.impl.CategoryServiceImpl;
import lt_web.utils.Constant;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/admin/category/delete"})
public class CategoryDeleteController extends HttpServlet {
    private final CategoryService cateService = new CategoryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = parseId(req.getParameter("id"));
        Category category = id > 0 ? cateService.get(id) : null;
        if (category == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list?categoryError=missing");
            return;
        }
        req.setAttribute("category", category);
        req.getRequestDispatcher(Constant.Path.ADMIN_DELETE_CATEGORY).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = parseId(req.getParameter("id"));
        try {
            if (id <= 0 || cateService.get(id) == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/category/list?categoryError=missing");
                return;
            }
            cateService.delete(id);
            resp.sendRedirect(req.getContextPath() + "/admin/category/list?categoryDeleted=1");
        } catch (DataAccessException e) {
            log("Không thể xóa danh mục", e);
            resp.sendRedirect(req.getContextPath() + "/admin/category/list?categoryError=delete");
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
