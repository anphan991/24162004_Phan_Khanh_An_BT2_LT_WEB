package lt_web.controllers.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import lt_web.entity.Category;
import lt_web.services.CategoryService;
import lt_web.services.impl.CategoryServiceImpl;
import lt_web.utils.Constant;
import lt_web.utils.FileStorage;
import lt_web.utils.ValidationUtil;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/admin/category/edit"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 25
)
public class CategoryEditController extends HttpServlet {
    private final CategoryService cateService = new CategoryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = req.getParameter("id");
        int categoryId = parseId(id);
        if (categoryId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list");
            return;
        }
        Category category = cateService.get(categoryId);
        if (category == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list");
            return;
        }
        req.setAttribute("category", category);
        RequestDispatcher dispatcher = req.getRequestDispatcher(Constant.Path.ADMIN_EDIT_CATEGORY);
        dispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Category category = new Category();
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            idParam = req.getParameter("cateid");
        }
        int cateid = parseId(idParam);
        if (cateid <= 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list");
            return;
        }
        String catename = req.getParameter("catename");
        category.setCateId(cateid);
        category.setCateName(catename == null ? "" : catename.trim());

        Category cateold = cateService.get(cateid);
        if (cateold == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list");
            return;
        }
        if (category.getName().isBlank()) {
            req.setAttribute("category", cateold);
            req.setAttribute("alert", "Tên danh mục không được để trống.");
            req.getRequestDispatcher(Constant.Path.ADMIN_EDIT_CATEGORY).forward(req, resp);
            return;
        }
        String fileold = cateold != null ? cateold.getIcon() : "default-category.svg";

        try {
            Part part = req.getPart("icon");
            String storedName = FileStorage.saveImage(part);
            category.setIcon(storedName == null ? fileold : storedName);
        } catch (Exception e) {
            log("Không thể lưu ảnh danh mục", e);
            req.setAttribute("category", cateold);
            req.setAttribute("alert", "Không thể lưu ảnh. Vui lòng chọn JPG, PNG, SVG, GIF hoặc WEBP hợp lệ.");
            req.getRequestDispatcher(Constant.Path.ADMIN_EDIT_CATEGORY).forward(req, resp);
            return;
        }

        String validationMessage = ValidationUtil.firstMessage(category);
        if (validationMessage != null) {
            req.setAttribute("category", cateold);
            req.setAttribute("alert", validationMessage);
            req.getRequestDispatcher(Constant.Path.ADMIN_EDIT_CATEGORY).forward(req, resp);
            return;
        }

        cateService.edit(category);
        resp.sendRedirect(req.getContextPath() + "/admin/category/list");
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
