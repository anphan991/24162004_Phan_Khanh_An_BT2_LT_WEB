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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lt_web.entity.User;
import lt_web.exceptions.DataAccessException;
import lt_web.services.OtpService;
import lt_web.services.UserService;
import lt_web.services.impl.OtpServiceImpl;
import lt_web.services.impl.UserServiceImpl;
import lt_web.utils.CloudinaryService;
import lt_web.utils.Constant;
import lt_web.utils.FileStorage;
import lt_web.utils.ValidationUtil;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/profile"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 10
)
public class ProfileController extends HttpServlet {
    private static final Set<String> ALLOWED_IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "svg", "webp");
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private final UserService userService = new UserServiceImpl();
    private final OtpService otpService = new OtpServiceImpl();
    private final CloudinaryService cloudinaryService = new CloudinaryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = resolveCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }
        forwardProfile(req, resp, currentUser, null, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        User currentUser = resolveCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?requireAuth=1");
            return;
        }

        String action = req.getParameter("action");
        if ("verifyEmailOtp".equals(action)) {
            handleVerifyEmailOtp(req, resp, currentUser);
            return;
        }
        if ("resendEmailOtp".equals(action)) {
            handleResendEmailOtp(req, resp, currentUser);
            return;
        }
        if ("cancelEmailOtp".equals(action)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.removeAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL);
            }
            forwardProfile(req, resp, currentUser, null, null, "Đã hủy yêu cầu đổi địa chỉ email.");
            return;
        }

        handleUpdateProfile(req, resp, currentUser);
    }

    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws ServletException, IOException {
        String fullname = trimOrEmpty(req.getParameter("fullname"));
        String phone = trimOrEmpty(req.getParameter("phone"));
        String email = trimOrEmpty(req.getParameter("email")).toLowerCase(Locale.ROOT);
        String currentEmail = trimOrEmpty(currentUser.getEmail()).toLowerCase(Locale.ROOT);

        Map<String, String> errors = new LinkedHashMap<>();

        if (fullname.isEmpty()) {
            errors.put("fullname", "Vui lòng nhập họ và tên.");
        } else if (fullname.length() < 2 || fullname.length() > 100) {
            errors.put("fullname", "Họ và tên phải từ 2 đến 100 ký tự.");
        }

        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            errors.put("phone", "Số điện thoại không hợp lệ (gồm 9-15 chữ số).");
        }

        if (email.isEmpty()) {
            errors.put("email", "Vui lòng nhập email.");
        } else if (!ValidationUtil.isValidEmail(email)) {
            errors.put("email", "Địa chỉ email không đúng định dạng.");
        }

        Part imagePart = findImagePart(req);
        if (imagePart != null && imagePart.getSize() > 0) {
            if (imagePart.getSize() > MAX_IMAGE_BYTES) {
                errors.put("images", "Kích thước ảnh đại diện tối đa là 5MB.");
            } else {
                String ext = extensionOf(FileStorage.getFileName(imagePart));
                if (!ALLOWED_IMAGE_EXT.contains(ext)) {
                    errors.put("images", "Chỉ hỗ trợ định dạng ảnh JPG, PNG, GIF, SVG hoặc WEBP.");
                }
            }
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("draftFullname", fullname);
            req.setAttribute("draftPhone", phone);
            req.setAttribute("draftEmail", email);
            forwardProfile(req, resp, currentUser, errors.values().iterator().next(), null, null);
            return;
        }

        try {
            if (!phone.isEmpty() && userService.checkExistPhone(phone, currentUser.getId())) {
                errors.put("phone", "Số điện thoại này đã được sử dụng bởi tài khoản khác.");
            }
            if (!email.equals(currentEmail) && userService.checkExistEmail(email, currentUser.getId())) {
                errors.put("email", "Email này đã được sử dụng bởi tài khoản khác.");
            }
            if (!errors.isEmpty()) {
                req.setAttribute("errors", errors);
                req.setAttribute("draftFullname", fullname);
                req.setAttribute("draftPhone", phone);
                req.setAttribute("draftEmail", email);
                forwardProfile(req, resp, currentUser, errors.values().iterator().next(), null, null);
                return;
            }

            String newAvatar = null;
            boolean uploadedToCloudinary = false;
            boolean usedLocalFallback = false;

            if (imagePart != null && imagePart.getSize() > 0) {
                if (cloudinaryService.isConfigured()) {
                    CloudinaryService.UploadResult uploadResult = cloudinaryService.uploadImage(imagePart);
                    if (!uploadResult.isSuccess()) {
                        errors.put("images", uploadResult.getErrorMessage());
                        req.setAttribute("errors", errors);
                        req.setAttribute("draftFullname", fullname);
                        req.setAttribute("draftPhone", phone);
                        req.setAttribute("draftEmail", email);
                        forwardProfile(req, resp, currentUser, uploadResult.getErrorMessage(), null, null);
                        return;
                    }
                    if (uploadResult.getSecureUrl().length() > 255) {
                        String msg = "URL ảnh Cloudinary vượt quá 255 ký tự.";
                        errors.put("images", msg);
                        req.setAttribute("errors", errors);
                        forwardProfile(req, resp, currentUser, msg, null, null);
                        return;
                    }
                    newAvatar = uploadResult.getSecureUrl();
                    uploadedToCloudinary = true;
                } else {
                    newAvatar = FileStorage.saveImage(imagePart);
                    usedLocalFallback = true;
                }
            }

            String oldAvatar = currentUser.getAvatar();
            User updatedUser = userService.updateProfile(currentUser.getId(), fullname, phone, newAvatar);
            if (updatedUser == null) {
                forwardProfile(req, resp, currentUser, "Không tìm thấy tài khoản trong cơ sở dữ liệu MySQL. Vui lòng đăng nhập lại.", null, null);
                return;
            }
            if (newAvatar != null && !newAvatar.equals(oldAvatar)) {
                FileStorage.deleteQuietly(oldAvatar);
            }
            HttpSession session = req.getSession(true);
            session.setAttribute(Constant.SESSION_ACCOUNT, updatedUser);
            currentUser = updatedUser;

            String avatarNote = "";
            if (uploadedToCloudinary) {
                avatarNote = " Ảnh đại diện đã được tải lên Cloudinary.";
            } else if (usedLocalFallback) {
                avatarNote = " Ảnh đại diện đã lưu cục bộ (chưa cấu hình Cloudinary trong db.properties hoặc biến môi trường).";
            }

            if (!email.equals(currentEmail)) {
                session.setAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL, email);
                otpService.generateAndSendEmailChangeOtp(email, fullname);
                String notice = "Đã cập nhật họ tên / số điện thoại"
                        + (avatarNote.isEmpty() ? "." : "." + avatarNote)
                        + " Hệ thống đã gửi mã OTP 6 chữ số đến email mới (" + email
                        + "). Vui lòng nhập mã OTP bên dưới để xác nhận thay đổi email.";
                forwardProfile(req, resp, currentUser, null, null, notice);
                return;
            }

            session.removeAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL);
            forwardProfile(req, resp, currentUser, null, "Cập nhật thông tin hồ sơ thành công!" + avatarNote, null);
        } catch (DataAccessException e) {
            log("Lỗi cơ sở dữ liệu khi cập nhật hồ sơ cá nhân", e);
            forwardProfile(req, resp, currentUser, "Không thể lưu thông tin vào cơ sở dữ liệu. Vui lòng thử lại.", null, null);
        } catch (IllegalStateException e) {
            log("Ảnh đại diện vượt dung lượng tối đa", e);
            errors.put("images", "Ảnh đại diện vượt quá dung lượng tối đa 5MB.");
            req.setAttribute("errors", errors);
            forwardProfile(req, resp, currentUser, errors.get("images"), null, null);
        } catch (Exception e) {
            log("Không thể cập nhật hồ sơ cá nhân", e);
            forwardProfile(req, resp, currentUser, "Không thể xử lý yêu cầu cập nhật hồ sơ. Vui lòng thử lại.", null, null);
        }
    }

    private void handleVerifyEmailOtp(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String pendingEmail = session != null
                ? trimOrEmpty((String) session.getAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL))
                : "";
        if (pendingEmail.isEmpty()) {
            forwardProfile(req, resp, currentUser, "Không có yêu cầu đổi email nào đang chờ xác nhận.", null, null);
            return;
        }

        String otp = trimOrEmpty(req.getParameter("otp"));
        Map<String, String> errors = new LinkedHashMap<>();
        if (otp.isEmpty()) {
            errors.put("otp", "Vui lòng nhập mã OTP gồm 6 chữ số.");
        } else if (!ValidationUtil.isValidOtp(otp)) {
            errors.put("otp", "Mã OTP phải gồm đúng 6 chữ số.");
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            forwardProfile(req, resp, currentUser, errors.get("otp"), null, null);
            return;
        }

        try {
            if (userService.checkExistEmail(pendingEmail, currentUser.getId())) {
                session.removeAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL);
                forwardProfile(req, resp, currentUser, "Email mới đã được tài khoản khác sử dụng. Vui lòng nhập email khác.", null, null);
                return;
            }

            if (!otpService.verifyEmailChangeOtp(pendingEmail, otp)) {
                errors.put("otp", "Mã OTP không chính xác hoặc đã hết hạn (5 phút).");
                req.setAttribute("errors", errors);
                forwardProfile(req, resp, currentUser, errors.get("otp"), null, null);
                return;
            }

            User updated = userService.updateEmail(currentUser.getId(), pendingEmail);
            if (updated != null) {
                session.setAttribute(Constant.SESSION_ACCOUNT, updated);
                currentUser = updated;
            }
            session.removeAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL);
            forwardProfile(req, resp, currentUser, null,
                    "Xác nhận OTP thành công! Email mới (" + pendingEmail + ") đã được cập nhật.", null);
        } catch (DataAccessException e) {
            log("Lỗi cơ sở dữ liệu khi xác nhận OTP đổi email", e);
            forwardProfile(req, resp, currentUser, "Không thể hoàn tất đổi email. Vui lòng thử lại.", null, null);
        }
    }

    private void handleResendEmailOtp(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String pendingEmail = session != null
                ? trimOrEmpty((String) session.getAttribute(Constant.SESSION_PENDING_PROFILE_EMAIL))
                : "";
        if (pendingEmail.isEmpty()) {
            forwardProfile(req, resp, currentUser, "Không có email mới đang chờ xác nhận.", null, null);
            return;
        }
        try {
            otpService.generateAndSendEmailChangeOtp(pendingEmail, currentUser.getFullName());
            forwardProfile(req, resp, currentUser, null, null,
                    "Đã gửi lại mã OTP 6 chữ số đến " + pendingEmail + ".");
        } catch (DataAccessException e) {
            log("Lỗi khi gửi lại OTP đổi email", e);
            forwardProfile(req, resp, currentUser, "Không thể gửi lại mã OTP lúc này. Vui lòng thử lại.", null, null);
        }
    }

    private User resolveCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        User sessionUser = session != null ? (User) session.getAttribute(Constant.SESSION_ACCOUNT) : null;
        if (sessionUser == null) {
            return null;
        }
        User fresh = userService.get(sessionUser.getId());
        if (fresh == null && sessionUser.getUserName() != null && !sessionUser.getUserName().isBlank()) {
            fresh = userService.get(sessionUser.getUserName());
        }
        if (fresh != null) {
            session.setAttribute(Constant.SESSION_ACCOUNT, fresh);
            session.setAttribute(Constant.SESSION_USERNAME, fresh.getUserName());
            return fresh;
        }
        session.invalidate();
        return null;
    }

    private void forwardProfile(HttpServletRequest req, HttpServletResponse resp, User currentUser,
                                String alert, String successMsg, String infoMsg)
            throws ServletException, IOException {
        req.setAttribute("profileUser", currentUser);
        req.setAttribute("cloudinaryConfigured", cloudinaryService.isConfigured());
        req.setAttribute("cloudinaryCloudName", cloudinaryService.getCloudName());
        if (alert != null) {
            req.setAttribute("alert", alert);
        }
        if (successMsg != null) {
            req.setAttribute("successMsg", successMsg);
        }
        if (infoMsg != null) {
            req.setAttribute("infoMsg", infoMsg);
        }
        req.getRequestDispatcher(Constant.Path.PROFILE).forward(req, resp);
    }

    private Part findImagePart(HttpServletRequest req) throws IOException, ServletException {
        Part part = req.getPart("images");
        if (part != null && part.getSize() > 0) {
            return part;
        }
        Part fallback = req.getPart("avatar");
        return (fallback != null && fallback.getSize() > 0) ? fallback : null;
    }

    private String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
