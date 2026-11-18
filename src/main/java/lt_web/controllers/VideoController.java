package lt_web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.List;
import lt_web.entity.User;
import lt_web.entity.Video;
import lt_web.exceptions.DataAccessException;
import lt_web.services.UserService;
import lt_web.services.VideoService;
import lt_web.services.impl.UserServiceImpl;
import lt_web.services.impl.VideoServiceImpl;
import lt_web.utils.Constant;
import lt_web.utils.FileStorage;
import lt_web.utils.ValidationUtil;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {
        "/video",
        "/video/detail",
        "/video/add",
        "/video/edit",
        "/video/delete",
        "/video/clear-samples",
        "/admin/video/list",
        "/admin/video/add",
        "/admin/video/edit",
        "/admin/video/delete",
        "/admin/video/clear-samples"
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 100,
        maxRequestSize = 1024 * 1024 * 120
)
public class VideoController extends HttpServlet {
    public static final int PAGE_SIZE = 6;

    private final VideoService videoService = new VideoServiceImpl();
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();

        if ("/video/detail".equals(path)) {
            handleDetail(req, resp);
        } else if ("/video/add".equals(path) || "/admin/video/add".equals(path)) {
            handleAddForm(req, resp);
        } else if ("/video/edit".equals(path) || "/admin/video/edit".equals(path)) {
            handleEditForm(req, resp);
        } else {
            handleList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();

        if ("/video/add".equals(path) || "/admin/video/add".equals(path)) {
            handleAddSubmit(req, resp);
        } else if ("/video/edit".equals(path) || "/admin/video/edit".equals(path)) {
            handleEditSubmit(req, resp);
        } else if ("/video/delete".equals(path) || "/admin/video/delete".equals(path)) {
            handleDelete(req, resp);
        } else if ("/video/clear-samples".equals(path) || "/admin/video/clear-samples".equals(path)) {
            handleClearSamples(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/video");
        }
    }

    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User account = resolveAccount(req);
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }
        String keyword = trimOrNull(req.getParameter("keyword"));
        String scopeParam = trimOrNull(req.getParameter("scope"));
        int uidParam = parsePositiveInt(req.getParameter("uid"));
        int requestedPage = Math.max(parsePositiveInt(req.getParameter("page")), 1);

        Integer filterUserId = null;
        String effectiveScope = "all";
        boolean showingAllFallback = false;

        try {
            if (uidParam > 0) {
                filterUserId = uidParam;
                effectiveScope = "user";
            } else if (account != null) {
                if ("all".equalsIgnoreCase(scopeParam)) {
                    effectiveScope = "all";
                } else if ("mine".equalsIgnoreCase(scopeParam)) {
                    filterUserId = account.getId();
                    effectiveScope = "mine";
                } else {
                    int myCount = videoService.countByUserId(account.getId());
                    if (myCount > 0) {
                        filterUserId = account.getId();
                        effectiveScope = "mine";
                    } else {
                        effectiveScope = "all";
                        showingAllFallback = true;
                    }
                }
            }

            int totalVideos = videoService.countSearch(keyword, filterUserId);
            int totalPages = Math.max((int) Math.ceil(totalVideos / (double) PAGE_SIZE), 1);
            int currentPage = Math.min(requestedPage, totalPages);
            int pageOffset = (currentPage - 1) * PAGE_SIZE;

            List<Video> videoList = videoService.search(keyword, filterUserId, currentPage, PAGE_SIZE);
            boolean hasSampleVideos = videoService.findAll().stream()
                    .anyMatch(v -> v.isSampleVideo() && canManageVideo(account, v));
            req.setAttribute("videoList", videoList);
            req.setAttribute("currentPage", currentPage);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalVideos", totalVideos);
            req.setAttribute("pageOffset", pageOffset);
            req.setAttribute("keyword", keyword);
            req.setAttribute("scope", effectiveScope);
            req.setAttribute("selectedUserId", filterUserId);
            req.setAttribute("showingAllFallback", showingAllFallback);
            req.setAttribute("hasSampleVideos", hasSampleVideos);
        } catch (DataAccessException e) {
            log("Không thể tải danh sách video", e);
            req.setAttribute("videoList", List.of());
            req.setAttribute("currentPage", 1);
            req.setAttribute("totalPages", 1);
            req.setAttribute("totalVideos", 0);
            req.setAttribute("pageOffset", 0);
            req.setAttribute("keyword", keyword);
            req.setAttribute("scope", effectiveScope);
            req.setAttribute("hasSampleVideos", false);
            req.setAttribute("dbError", "Không thể tải danh sách video. Hãy kiểm tra kết nối MySQL.");
        }

        req.getRequestDispatcher(Constant.Path.VIDEO_LIST).forward(req, resp);
    }

    private void handleDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User account = resolveAccount(req);
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }
        int id = parsePositiveInt(firstNonBlank(req.getParameter("id"), req.getParameter("videoId")));
        if (id <= 0) {
            resp.sendRedirect(req.getContextPath() + "/video?videoError=missing");
            return;
        }

        try {
            videoService.incrementViews(id);
            Video video = videoService.findById(id);
            if (video == null) {
                resp.sendRedirect(req.getContextPath() + "/video?videoError=missing");
                return;
            }
            List<Video> relatedVideos = videoService.findLatest(6);
            req.setAttribute("video", video);
            req.setAttribute("relatedVideos", relatedVideos);
            req.setAttribute("canManage", canManageVideo(account, video));
            req.getRequestDispatcher(Constant.Path.VIDEO_DETAIL).forward(req, resp);
        } catch (DataAccessException e) {
            log("Không thể tải chi tiết video", e);
            resp.sendRedirect(req.getContextPath() + "/video?videoError=load");
        }
    }

    private void handleAddForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User account = resolveAccount(req);
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }
        Video video = new Video();
        video.setActive(1);
        showForm(req, resp, video, "add", null);
    }

    private void handleAddSubmit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User account = resolveAccount(req);
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }

        String title = trimOrEmpty(req.getParameter("title"));
        String description = trimOrNull(req.getParameter("description"));
        String videoUrl = trimOrNull(req.getParameter("videoUrl"));
        int active = parseActive(req.getParameter("active"));

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setActive(active);
        video.setUser(account);

        String validationMessage = ValidationUtil.firstMessage(video);
        if (validationMessage != null) {
            video.setVideoPath(videoUrl);
            showForm(req, resp, video, "add", validationMessage);
            return;
        }

        String storedPoster = null;
        String storedVideo = null;
        try {
            Part posterPart = req.getPart("poster");
            storedPoster = FileStorage.saveImage(posterPart);
            video.setPoster(storedPoster != null ? storedPoster : "default-video-poster.svg");

            Part videoPart = firstNonEmptyPart(req, "videoFile", "multiPartServlet", "uploadFile");
            storedVideo = FileStorage.saveVideo(videoPart);
            if (storedVideo != null) {
                video.setVideoPath(storedVideo);
            } else if (videoUrl != null) {
                if (!isValidExternalUrl(videoUrl)) {
                    cleanupUploadedFiles(storedPoster, null);
                    showForm(req, resp, video, "add", "Đường dẫn video URL phải bắt đầu bằng http:// hoặc https://.");
                    return;
                }
                video.setVideoPath(videoUrl);
            } else {
                video.setVideoPath("default-video.mp4");
            }
        } catch (IllegalStateException e) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            log("Tệp tải lên vượt quá dung lượng cho phép", e);
            showForm(req, resp, video, "add", "Dung lượng tệp tải lên vượt quá giới hạn cho phép (tối đa 100MB).");
            return;
        } catch (Exception e) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            log("Không thể lưu tệp upload cho video", e);
            showForm(req, resp, video, "add", e.getMessage() != null ? e.getMessage() : "Không thể tải tệp lên.");
            return;
        }

        validationMessage = ValidationUtil.firstMessage(video);
        if (validationMessage != null) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            showForm(req, resp, video, "add", validationMessage);
            return;
        }

        try {
            videoService.create(video);
            resp.sendRedirect(req.getContextPath() + "/video?saved=1");
        } catch (DataAccessException e) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            log("Không thể lưu video mới vào cơ sở dữ liệu", e);
            showForm(req, resp, video, "add", "Không thể lưu video vào cơ sở dữ liệu. Vui lòng thử lại.");
        }
    }

    private void handleEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User account = resolveAccount(req);
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }

        int id = parsePositiveInt(req.getParameter("id"));
        if (id <= 0) {
            resp.sendRedirect(req.getContextPath() + "/video?videoError=missing");
            return;
        }

        try {
            Video existing = videoService.findById(id);
            if (existing == null) {
                resp.sendRedirect(req.getContextPath() + "/video?videoError=missing");
                return;
            }
            if (!canManageVideo(account, existing)) {
                resp.sendRedirect(req.getContextPath() + "/video?videoError=forbidden");
                return;
            }
            showForm(req, resp, existing, "edit", null);
        } catch (DataAccessException e) {
            log("Không thể tải video để chỉnh sửa", e);
            resp.sendRedirect(req.getContextPath() + "/video?videoError=load");
        }
    }

    private void handleEditSubmit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User account = resolveAccount(req);
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }

        int id = parsePositiveInt(firstNonBlank(req.getParameter("id"), req.getParameter("videoId")));
        if (id <= 0) {
            resp.sendRedirect(req.getContextPath() + "/video?videoError=missing");
            return;
        }

        Video existing;
        try {
            existing = videoService.findById(id);
        } catch (DataAccessException e) {
            log("Không thể tải video cần cập nhật", e);
            resp.sendRedirect(req.getContextPath() + "/video?videoError=load");
            return;
        }

        if (existing == null) {
            resp.sendRedirect(req.getContextPath() + "/video?videoError=missing");
            return;
        }
        if (!canManageVideo(account, existing)) {
            resp.sendRedirect(req.getContextPath() + "/video?videoError=forbidden");
            return;
        }

        String title = trimOrEmpty(req.getParameter("title"));
        String description = trimOrNull(req.getParameter("description"));
        String videoUrl = trimOrNull(req.getParameter("videoUrl"));
        int active = parseActive(req.getParameter("active"));

        Video draft = new Video();
        draft.setVideoId(existing.getVideoId());
        draft.setTitle(title);
        draft.setDescription(description);
        draft.setActive(active);
        draft.setPoster(existing.getPoster());
        draft.setVideoPath(existing.getVideoPath());
        draft.setUser(existing.getUser());

        String validationMessage = ValidationUtil.firstMessage(draft);
        if (validationMessage != null) {
            showForm(req, resp, draft, "edit", validationMessage);
            return;
        }

        String storedPoster = null;
        String storedVideo = null;
        try {
            Part posterPart = req.getPart("poster");
            storedPoster = FileStorage.saveImage(posterPart);

            Part videoPart = firstNonEmptyPart(req, "videoFile", "multiPartServlet", "uploadFile");
            storedVideo = FileStorage.saveVideo(videoPart);

            Video updated = new Video();
            updated.setVideoId(existing.getVideoId());
            updated.setTitle(title);
            updated.setDescription(description);
            updated.setActive(active);
            updated.setUser(existing.getUser());
            if (storedPoster != null) {
                updated.setPoster(storedPoster);
            }
            if (storedVideo != null) {
                updated.setVideoPath(storedVideo);
            } else if (videoUrl != null && !videoUrl.equals(existing.getVideoPath())) {
                if (!isValidExternalUrl(videoUrl)) {
                    cleanupUploadedFiles(storedPoster, null);
                    showForm(req, resp, draft, "edit", "Đường dẫn video URL phải bắt đầu bằng http:// hoặc https://.");
                    return;
                }
                updated.setVideoPath(videoUrl);
            }

            validationMessage = ValidationUtil.firstMessage(updated);
            if (validationMessage != null) {
                cleanupUploadedFiles(storedPoster, storedVideo);
                showForm(req, resp, draft, "edit", validationMessage);
                return;
            }

            videoService.update(updated);
            resp.sendRedirect(req.getContextPath() + "/video?updated=1");
        } catch (IllegalStateException e) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            log("Tệp tải lên vượt quá dung lượng cho phép", e);
            showForm(req, resp, draft, "edit", "Dung lượng tệp tải lên vượt quá giới hạn cho phép (tối đa 100MB).");
        } catch (DataAccessException e) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            log("Không thể cập nhật video", e);
            showForm(req, resp, draft, "edit", "Không thể cập nhật video vào cơ sở dữ liệu. Vui lòng thử lại.");
        } catch (Exception e) {
            cleanupUploadedFiles(storedPoster, storedVideo);
            log("Lỗi khi xử lý tệp upload chỉnh sửa video", e);
            showForm(req, resp, draft, "edit", e.getMessage() != null ? e.getMessage() : "Không thể xử lý tệp tải lên.");
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        User account = resolveAccount(req);
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }

        int id = parsePositiveInt(req.getParameter("id"));
        if (id <= 0) {
            resp.sendRedirect(req.getContextPath() + "/video?videoError=missing");
            return;
        }

        try {
            Video existing = videoService.findById(id);
            if (existing == null) {
                resp.sendRedirect(req.getContextPath() + "/video?videoError=missing");
                return;
            }
            if (!canManageVideo(account, existing)) {
                resp.sendRedirect(req.getContextPath() + "/video?videoError=forbidden");
                return;
            }
            videoService.delete(id);
            resp.sendRedirect(req.getContextPath() + "/video?deleted=1");
        } catch (DataAccessException e) {
            log("Không thể xóa video", e);
            resp.sendRedirect(req.getContextPath() + "/video?videoError=delete");
        }
    }

    private void handleClearSamples(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        User account = resolveAccount(req);
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }

        try {
            int deletedCount = 0;
            for (Video video : videoService.findAll()) {
                if (video.isSampleVideo() && canManageVideo(account, video)) {
                    videoService.delete(video.getVideoId());
                    deletedCount++;
                }
            }
            resp.sendRedirect(req.getContextPath() + "/video?clearedSamples=" + deletedCount);
        } catch (DataAccessException e) {
            log("Không thể xóa các video mẫu", e);
            resp.sendRedirect(req.getContextPath() + "/video?videoError=delete");
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Video video, String mode, String alert)
            throws ServletException, IOException {
        req.setAttribute("video", video);
        req.setAttribute("formMode", mode);
        if (alert != null) {
            req.setAttribute("alert", alert);
        }
        req.getRequestDispatcher(Constant.Path.VIDEO_FORM).forward(req, resp);
    }

    private User resolveAccount(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object accountObj = session.getAttribute(Constant.SESSION_ACCOUNT);
        if (accountObj instanceof User user) {
            return user;
        }
        Object usernameObj = session.getAttribute(Constant.SESSION_USERNAME);
        if (usernameObj instanceof String username && !username.isBlank()) {
            User loaded = userService.get(username);
            if (loaded != null) {
                session.setAttribute(Constant.SESSION_ACCOUNT, loaded);
                return loaded;
            }
        }
        return null;
    }

    private boolean canManageVideo(User account, Video video) {
        if (account == null || video == null) {
            return false;
        }
        if (account.getRoleid() == 1 || account.getRoleid() == 2) {
            return true;
        }
        return video.getUser() != null && video.getUser().getId() == account.getId();
    }

    private Part firstNonEmptyPart(HttpServletRequest req, String... names) throws IOException, ServletException {
        for (String name : names) {
            Part part = req.getPart(name);
            if (part != null && part.getSize() > 0) {
                return part;
            }
        }
        return null;
    }

    private void cleanupUploadedFiles(String posterFile, String videoFile) {
        FileStorage.deleteQuietly(posterFile);
        FileStorage.deleteQuietly(videoFile);
    }

    private boolean isValidExternalUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private int parsePositiveInt(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int parseActive(String value) {
        return "0".equals(trimOrEmpty(value)) ? 0 : 1;
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
