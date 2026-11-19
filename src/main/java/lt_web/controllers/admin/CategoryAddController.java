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
import java.util.LinkedHashMap;
import java.util.Map;
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
        String trimmedName = categoryname == null ? "" : categoryname.trim();
        category.setCateName(trimmedName);

        Map<String, String> errors = new LinkedHashMap<>(ValidationUtil.fieldErrors(category));
        if (!errors.containsKey("catename") && trimmedName.length() < 2) {
            errors.put("catename", "Tên danh mục phải có ít nhất 2 ký tự.");
        }
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            showFormError(req, resp, trimmedName, errors.values().iterator().next());
            return;
        }

        String storedName = null;
        try {
            Part part = req.getPart("icon");
            if (part == null || part.getSize() == 0) {
                part = req.getPart("images");
            }
            if (part != null && part.getSize() > 5L * 1024 * 1024) {
                errors.put("icon", "Kích thước ảnh tối đa là 5MB.");
                req.setAttribute("errors", errors);
                showFormError(req, resp, trimmedName, errors.get("icon"));
                return;
            }
            storedName = FileStorage.saveImage(part);
            category.setIcon(storedName == null ? "default-category.svg" : storedName);
        } catch (IllegalStateException e) {
            log("Ảnh danh mục vượt dung lượng cho phép", e);
            errors.put("icon", "Kích thước ảnh vượt quá giới hạn tối đa (5MB).");
            req.setAttribute("errors", errors);
            showFormError(req, resp, trimmedName, errors.get("icon"));
            return;
        } catch (Exception e) {
            log("Không thể lưu ảnh danh mục", e);
            errors.put("icon", "Không thể lưu ảnh. Vui lòng chọn JPG, PNG, SVG, GIF hoặc WEBP hợp lệ.");
            req.setAttribute("errors", errors);
            showFormError(req, resp, trimmedName, errors.get("icon"));
            return;
        }

        String validationMessage = ValidationUtil.firstMessage(category);
        if (validationMessage != null) {
            FileStorage.deleteQuietly(storedName);
            req.setAttribute("errors", ValidationUtil.fieldErrors(category));
            showFormError(req, resp, trimmedName, validationMessage);
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
