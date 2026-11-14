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
import lt_web.exceptions.DataAccessException;
import lt_web.services.CategoryService;
import lt_web.services.impl.CategoryServiceImpl;
import lt_web.utils.Constant;
import lt_web.utils.FileStorage;
import lt_web.utils.ValidationUtil;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/admin/category/add"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 25
)
public class CategoryAddController extends HttpServlet {
    private final CategoryService cateService = new CategoryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = req.getRequestDispatcher(Constant.Path.ADMIN_ADD_CATEGORY);
        dispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Category category = new Category();
        String categoryname = req.getParameter("catename");
        category.setCateName(categoryname == null ? "" : categoryname.trim());
        String validationMessage = ValidationUtil.firstMessage(category);
        if (validationMessage != null) {
            showFormError(req, resp, category.getCatename(), validationMessage);
            return;
        }

        String storedName = null;
        try {
            Part part = req.getPart("icon");
            if (part == null || part.getSize() == 0) {
                part = req.getPart("images");
            }
            storedName = FileStorage.saveImage(part);
            category.setIcon(storedName == null ? "default-category.svg" : storedName);
        } catch (Exception e) {
            log("Không thể lưu ảnh danh mục", e);
            showFormError(req, resp, category.getCatename(),
                    "Không thể lưu ảnh. Vui lòng chọn JPG, PNG, SVG, GIF hoặc WEBP hợp lệ.");
            return;
        }

        validationMessage = ValidationUtil.firstMessage(category);
        if (validationMessage != null) {
            FileStorage.deleteQuietly(storedName);
            showFormError(req, resp, category.getCatename(), validationMessage);
            return;
        }

        try {
            cateService.insert(category);
            resp.sendRedirect(req.getContextPath() + "/admin/category/list");
        } catch (DataAccessException e) {
            FileStorage.deleteQuietly(storedName);
            log("Không thể thêm danh mục", e);
            showFormError(req, resp, category.getCatename(), "Không thể lưu danh mục. Vui lòng thử lại.");
        }
    }

    private void showFormError(HttpServletRequest req, HttpServletResponse resp, String catename, String alert)
            throws ServletException, IOException {
        req.setAttribute("catename", catename);
        req.setAttribute("alert", alert);
        req.getRequestDispatcher(Constant.Path.ADMIN_ADD_CATEGORY).forward(req, resp);
    }
}
