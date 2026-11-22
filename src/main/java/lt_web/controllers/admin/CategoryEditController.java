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
import lt_web.utils.CloudinaryService;
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
    private final CloudinaryService cloudinaryService = new CloudinaryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = req.getParameter("id");
        int categoryId = parseId(id);
        if (categoryId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list");
            return;
        }
        try {
            Category category = cateService.get(categoryId);
            if (category == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/category/list?categoryError=missing");
                return;
            }
            req.setAttribute("category", category);
            RequestDispatcher dispatcher = req.getRequestDispatcher(Constant.Path.ADMIN_EDIT_CATEGORY);
            dispatcher.forward(req, resp);
        } catch (DataAccessException e) {
            log("Không thể tải danh mục cần sửa", e);
            resp.sendRedirect(req.getContextPath() + "/admin/category/list?categoryError=load");
        }
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

        Category cateold;
        try {
            cateold = cateService.get(cateid);
        } catch (DataAccessException e) {
            log("Không thể tải danh mục cần cập nhật", e);
            resp.sendRedirect(req.getContextPath() + "/admin/category/list?categoryError=load");
            return;
        }
        if (cateold == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/category/list?categoryError=missing");
            return;
        }

        Category draft = new Category(cateold.getCateid(), category.getCatename(), cateold.getIcon());
        Map<String, String> errors = new LinkedHashMap<>(ValidationUtil.fieldErrors(category));
        if (!errors.containsKey("catename") && category.getCatename().length() < 2) {
            errors.put("catename", "Tên danh mục phải có ít nhất 2 ký tự.");
        }
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            showFormError(req, resp, draft, errors.values().iterator().next());
            return;
        }

        String fileold = cateold.getIcon() != null ? cateold.getIcon() : "default-category.svg";
        String storedName = null;
        try {
            Part part = req.getPart("icon");
            if (part != null && part.getSize() > 5L * 1024 * 1024) {
                errors.put("icon", "Kích thước ảnh tối đa là 5MB.");
                req.setAttribute("errors", errors);
                showFormError(req, resp, draft, errors.get("icon"));
                return;
            }
            storedName = cloudinaryService.saveImageWithCloudFallback(part, "shopmvc/categories", 255);
            category.setIcon(storedName == null ? fileold : storedName);
        } catch (IllegalStateException e) {
            log("Ảnh danh mục vượt dung lượng cho phép", e);
            errors.put("icon", "Kích thước ảnh vượt quá giới hạn tối đa (5MB).");
            req.setAttribute("errors", errors);
            showFormError(req, resp, draft, errors.get("icon"));
            return;
        } catch (Exception e) {
            log("Không thể lưu ảnh danh mục", e);
            String msg = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage()
                    : "Không thể lưu ảnh. Vui lòng chọn JPG, PNG, SVG, GIF hoặc WEBP hợp lệ.";
            errors.put("icon", msg);
            req.setAttribute("errors", errors);
            showFormError(req, resp, draft, msg);
            return;
        }

        String validationMessage = ValidationUtil.firstMessage(category);
        if (validationMessage != null) {
            FileStorage.deleteQuietly(storedName);
            req.setAttribute("errors", ValidationUtil.fieldErrors(category));
            showFormError(req, resp, draft, validationMessage);
            return;
        }

        try {
            cateService.edit(category);
            resp.sendRedirect(req.getContextPath() + "/admin/category/list");
        } catch (DataAccessException | IllegalStateException e) {
            FileStorage.deleteQuietly(storedName);
            log("Không thể cập nhật danh mục", e);
            showFormError(req, resp, draft, "Không thể cập nhật danh mục. Vui lòng thử lại.");
        }
    }

    private void showFormError(HttpServletRequest req, HttpServletResponse resp, Category cateold, String alert)
            throws ServletException, IOException {
        req.setAttribute("category", cateold);
        req.setAttribute("alert", alert);
        req.getRequestDispatcher(Constant.Path.ADMIN_EDIT_CATEGORY).forward(req, resp);
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
