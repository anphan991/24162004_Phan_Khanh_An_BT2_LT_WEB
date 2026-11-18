package lt_web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import lt_web.exceptions.DataAccessException;
import lt_web.entity.Category;
import lt_web.entity.User;
import lt_web.entity.Video;
import lt_web.services.CategoryService;
import lt_web.services.UserService;
import lt_web.services.VideoService;
import lt_web.services.impl.CategoryServiceImpl;
import lt_web.services.impl.UserServiceImpl;
import lt_web.services.impl.VideoServiceImpl;
import lt_web.utils.Constant;

@WebServlet(urlPatterns = {"/", "/home", "/admin", "/admin/home", "/manager/home"})
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final CategoryService cateService = new CategoryServiceImpl();
    private final VideoService videoService = new VideoServiceImpl();
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/admin".equals(path) || "/admin/home".equals(path) || "/manager/home".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/admin/category/list");
            return;
        }

        boolean authenticated = isAuthenticated(request);
        if (!authenticated && !"/home".equals(path)) {
            request.setAttribute("latestVideos", List.of());
            request.setAttribute("cateList", List.of());
            request.getRequestDispatcher("/views/index.jsp").forward(request, response);
            return;
        }

        List<Video> latestVideos;
        try {
            latestVideos = videoService.findLatest(10);
        } catch (DataAccessException e) {
            log("Không thể tải danh sách video mới nhất", e);
            latestVideos = List.of();
            request.setAttribute("videoDbError", "Không thể tải danh sách video mới nhất. Hãy kiểm tra kết nối MySQL.");
        }
        request.setAttribute("latestVideos", latestVideos);

        String keyword = request.getParameter("keyword");
        List<Category> cateList;
        try {
            if (keyword != null && !keyword.isBlank()) {
                cateList = cateService.search(keyword);
                request.setAttribute("keyword", keyword);
            } else {
                cateList = cateService.getAll();
            }
        } catch (DataAccessException e) {
            log("Không thể tải danh mục cho trang public", e);
            cateList = List.of();
            request.setAttribute("dbError", "Không thể tải danh mục. Hãy kiểm tra MySQL đang chạy và cơ sở dữ liệu bt_lt_web đã tồn tại.");
        }
        request.setAttribute("cateList", cateList);

        if ("/home".equals(path)) {
            request.getRequestDispatcher("/views/home.jsp")
                    .forward(request, response);
        } else {
            request.getRequestDispatcher("/views/index.jsp")
                    .forward(request, response);
        }
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        if (session.getAttribute(Constant.SESSION_ACCOUNT) instanceof User) {
            return true;
        }
        Object usernameObj = session.getAttribute(Constant.SESSION_USERNAME);
        if (usernameObj instanceof String username && !username.isBlank()) {
            User account = userService.get(username);
            if (account != null) {
                session.setAttribute(Constant.SESSION_ACCOUNT, account);
                session.setAttribute(Constant.SESSION_USERNAME, account.getUserName());
                return true;
            }
        }
        return false;
    }
}
