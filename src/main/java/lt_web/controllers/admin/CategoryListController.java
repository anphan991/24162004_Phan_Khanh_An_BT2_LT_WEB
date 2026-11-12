package lt_web.controllers.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lt_web.exceptions.DataAccessException;
import lt_web.entity.Category;
import lt_web.entity.User;
import lt_web.services.CategoryService;
import lt_web.services.UserService;
import lt_web.services.impl.CategoryServiceImpl;
import lt_web.services.impl.UserServiceImpl;
import lt_web.utils.Constant;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/admin/category/list"})
public class CategoryListController extends HttpServlet {
    private final CategoryService cateService = new CategoryServiceImpl();
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String keyword = req.getParameter("keyword");
        List<Category> cateList;
        try {
            if (keyword != null && !keyword.isBlank()) {
                cateList = cateService.search(keyword);
                req.setAttribute("keyword", keyword);
            } else {
                cateList = cateService.getAll();
            }
        } catch (DataAccessException e) {
            log("Không thể tải danh mục trong trang quản trị", e);
            cateList = List.of();
            req.setAttribute("dbError", "Không thể tải danh mục. Hãy kiểm tra MySQL đang chạy và cơ sở dữ liệu bt_lt_web đã tồn tại.");
        }
        List<User> userList;
        try {
            userList = userService.getAll();
        } catch (DataAccessException e) {
            log("Không thể tải tài khoản trong trang quản trị", e);
            userList = List.of();
            req.setAttribute("userError", "Không thể tải danh sách tài khoản. Hãy kiểm tra kết nối MySQL.");
        }
        req.setAttribute("cateList", cateList);
        req.setAttribute("userList", userList);
        RequestDispatcher dispatcher = req.getRequestDispatcher(Constant.Path.ADMIN_LIST_CATEGORY);
        dispatcher.forward(req, resp);
    }
}
