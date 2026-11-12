package lt_web.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class CsrfToken {
    public static final String SESSION_ATTRIBUTE = "csrfToken";
    private static final String REQUEST_PARAMETER = "csrfToken";

    private CsrfToken() {
    }

    public static String ensure(HttpSession session) {
        Object current = session.getAttribute(SESSION_ATTRIBUTE);
        if (current instanceof String token && !token.isBlank()) {
            return token;
        }
        String token = TokenUtil.randomToken();
        session.setAttribute(SESSION_ATTRIBUTE, token);
        return token;
    }

    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expectedValue = session.getAttribute(SESSION_ATTRIBUTE);
        String actual = request.getParameter(REQUEST_PARAMETER);
        if (!(expectedValue instanceof String expected) || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
