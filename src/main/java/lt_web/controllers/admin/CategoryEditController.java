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

        String validationMessage = ValidationUtil.firstMessage(category);
        if (validationMessage != null) {
            showFormError(req, resp, cateold, validationMessage);
            return;
        }

        String fileold = cateold.getIcon() != null ? cateold.getIcon() : "default-category.svg";
        String storedName = null;
        try {
            Part part = req.getPart("icon");
            storedName = FileStorage.saveImage(part);
            category.setIcon(storedName == null ? fileold : storedName);
        } catch (Exception e) {
            log("Không thể lưu ảnh danh mục", e);
            showFormError(req, resp, cateold,
                    "Không thể lưu ảnh. Vui lòng chọn JPG, PNG, SVG, GIF hoặc WEBP hợp lệ.");
            return;
        }

        validationMessage = ValidationUtil.firstMessage(category);
        if (validationMessage != null) {
            FileStorage.deleteQuietly(storedName);
            showFormError(req, resp, cateold, validationMessage);
            return;
        }

        try {
            cateService.edit(category);
            resp.sendRedirect(req.getContextPath() + "/admin/category/list");
        } catch (DataAccessException | IllegalStateException e) {
            FileStorage.deleteQuietly(storedName);
            log("Không thể cập nhật danh mục", e);
            showFormError(req, resp, cateold, "Không thể cập nhật danh mục. Vui lòng thử lại.");
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
