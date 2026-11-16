package lt_web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lt_web.entity.OtpToken;
import lt_web.services.OtpService;
import lt_web.services.UserService;
import lt_web.services.impl.OtpServiceImpl;
import lt_web.services.impl.UserServiceImpl;
import lt_web.utils.Constant;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/verify-otp")
public class VerifyOtpController extends HttpServlet {
    private final OtpService otpService = new OtpServiceImpl();
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = resolveEmail(req);
        String type = resolveType(req);

        if (email == null || email.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("email", email);
        req.setAttribute("type", type);
        req.getRequestDispatcher(Constant.Path.VERIFY_OTP).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String email = resolveEmail(req);
        String type = resolveType(req);
        String otpCode = req.getParameter("otp");

        if (email == null || email.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("email", email);
        req.setAttribute("type", type);

        if (otpCode == null || otpCode.trim().length() != 6) {
            req.setAttribute("alert", "Mã OTP phải gồm 6 chữ số.");
            req.getRequestDispatcher(Constant.Path.VERIFY_OTP).forward(req, resp);
            return;
        }

        String trimmedOtp = otpCode.trim();
        boolean isActivation = OtpToken.TYPE_ACTIVATION.equalsIgnoreCase(type);

        boolean verified = isActivation
                ? otpService.verifyActivationOtp(email, trimmedOtp)
                : otpService.verifyForgotPasswordOtp(email, trimmedOtp);

        if (verified) {
            HttpSession session = req.getSession(true);
            if (isActivation) {
                userService.activateUser(email);
                session.removeAttribute("activationEmail");
                resp.sendRedirect(req.getContextPath() + "/login?activated=1");
            } else {
                session.setAttribute("verifiedResetEmail", email.trim());
                String encodedEmail = URLEncoder.encode(email.trim(), StandardCharsets.UTF_8);
                resp.sendRedirect(req.getContextPath() + "/reset-password?email=" + encodedEmail);
            }
        } else {
            req.setAttribute("alert", "Mã OTP không đúng hoặc đã hết hạn (hiệu lực 5 phút). Vui lòng thử lại hoặc nhấn Gửi lại mã.");
            req.getRequestDispatcher(Constant.Path.VERIFY_OTP).forward(req, resp);
        }
    }

    private String resolveEmail(HttpServletRequest req) {
        String email = req.getParameter("email");
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object sessionEmail = session.getAttribute("activationEmail");
            if (sessionEmail == null) {
                sessionEmail = session.getAttribute("forgotEmail");
            }
            if (sessionEmail instanceof String s && !s.isBlank()) {
                return s.trim();
            }
        }
        return null;
    }

    private String resolveType(HttpServletRequest req) {
        String type = req.getParameter("type");
        if (type != null && !type.isBlank()) {
            if ("forgot".equalsIgnoreCase(type) || OtpToken.TYPE_FORGOT_PASSWORD.equalsIgnoreCase(type)) {
                return OtpToken.TYPE_FORGOT_PASSWORD;
            }
            return OtpToken.TYPE_ACTIVATION;
        }
        return OtpToken.TYPE_ACTIVATION;
    }
}
