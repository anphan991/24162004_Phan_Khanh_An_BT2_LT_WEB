package lt_web.utils;

import java.nio.file.Paths;

public class Constant {
    public static final String SESSION_USERNAME = "username";
    public static final String SESSION_ACCOUNT = "account";
    public static final String COOKIE_REMEMBER = "remember_token";
    public static final String LEGACY_COOKIE_REMEMBER = "username";

    private Constant() {
    }

    public static java.nio.file.Path uploadDirectory() {
        String configured = firstNonBlank(
                System.getProperty("bt.lt.upload.dir"),
                System.getenv("BT_LT_UPLOAD_DIR"));
        if (configured == null) {
            configured = Paths.get(System.getProperty("user.home"), "BT2_LT_Web", "uploads").toString();
        }
        return Paths.get(configured).toAbsolutePath().normalize();
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return null;
    }

    public static class Path {
        public static final String LOGIN = "/views/login.jsp";
        public static final String REGISTER = "/views/register.jsp";
        public static final String VERIFY_OTP = "/views/verify-otp.jsp";
        public static final String FORGOT_PASSWORD = "/views/forgot-password.jsp";
        public static final String RESET_PASSWORD = "/views/reset-password.jsp";
        public static final String HOME = "/views/home.jsp";
        public static final String ADMIN_LIST_CATEGORY = "/views/admin/list-category.jsp";
        public static final String ADMIN_ADD_CATEGORY = "/views/admin/add-category.jsp";
        public static final String ADMIN_EDIT_CATEGORY = "/views/admin/edit-category.jsp";
        public static final String ADMIN_DELETE_CATEGORY = "/views/admin/delete-category.jsp";
        public static final String ADMIN_USER_FORM = "/views/admin/user-form.jsp";
    }
}
