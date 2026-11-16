<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quên mật khẩu - ShopMVC</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css">
</head>
<body>
<jsp:include page="/views/topbar.jsp" />

<main class="auth-page">
    <section class="auth-card" aria-labelledby="forgot-title">
        <div class="auth-header">
            <h1 id="forgot-title">Quên mật khẩu</h1>
            <p>Nhập email đăng ký của bạn để nhận mã OTP đặt lại mật khẩu.</p>
        </div>

        <c:if test="${alert != null}">
            <div class="alert alert-danger"><c:out value="${alert}" /></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/forgot-password" method="post">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
            <div class="field-group">
                <label for="email">Email đăng ký</label>
                <input id="email" class="input-control" type="email" name="email"
                       value="<c:out value='${email}' />" placeholder="email@domain.com" required autofocus autocomplete="email">
            </div>

            <p class="form-note">Hệ thống sẽ gửi mã xác thực OTP 6 chữ số tới email của bạn (hiệu lực 5 phút).</p>

            <button class="button button-primary auth-submit" type="submit" style="margin-top: 14px;">Gửi mã OTP</button>
        </form>

        <div class="auth-note">
            Nhớ mật khẩu? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
        </div>
    </section>
</main>
</body>
</html>
