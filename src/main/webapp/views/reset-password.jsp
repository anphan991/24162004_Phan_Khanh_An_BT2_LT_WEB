<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đặt lại mật khẩu - ShopMVC</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css">
    <style>
        .reset-email-box {
            margin-bottom: 20px;
            padding: 12px 14px;
            background: #f7f9fc;
            border-left: 3px solid var(--accent);
            border-radius: 4px;
            font-size: 13.5px;
            color: #364152;
        }
        .otp-input-field {
            text-align: center;
            font-size: 22px;
            font-weight: 700;
            letter-spacing: 6px;
            font-family: monospace;
        }
    </style>
</head>
<body>
<jsp:include page="/views/topbar.jsp" />

<main class="auth-page">
    <section class="auth-card" aria-labelledby="reset-title">
        <div class="auth-header">
            <h1 id="reset-title">Đặt lại mật khẩu</h1>
            <p>Nhập mật khẩu mới để bảo mật tài khoản của bạn.</p>
        </div>

        <div class="reset-email-box">
            Tài khoản: <strong><c:out value="${email}" /></strong>
        </div>

        <c:if test="${alert != null}">
            <div class="alert alert-danger"><c:out value="${alert}" /></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/reset-password" method="post">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
            <input type="hidden" name="email" value="<c:out value='${email}' />">

            <c:if test="${!isOtpVerified}">
                <div class="field-group">
                    <label for="otp">Mã OTP (6 chữ số)</label>
                    <input id="otp" class="input-control otp-input-field" type="text" name="otp"
                           maxlength="6" pattern="[0-9]{6}" inputmode="numeric"
                           placeholder="000000" required autofocus autocomplete="one-time-code">
                </div>
            </c:if>

            <div class="field-group">
                <label for="password">Mật khẩu mới</label>
                <input id="password" class="input-control" type="password" name="password"
                       placeholder="Tối thiểu 3 ký tự" required autocomplete="new-password">
            </div>

            <div class="field-group">
                <label for="repassword">Nhập lại mật khẩu mới</label>
                <input id="repassword" class="input-control" type="password" name="repassword"
                       placeholder="Xác nhận mật khẩu mới" required autocomplete="new-password">
            </div>

            <button class="button button-primary auth-submit" type="submit" style="margin-top: 14px;">Đặt lại mật khẩu</button>
        </form>

        <div class="auth-note">
            Quay lại <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
        </div>
    </section>
</main>
</body>
</html>
