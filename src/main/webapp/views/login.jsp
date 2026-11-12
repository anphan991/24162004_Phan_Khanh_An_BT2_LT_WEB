<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập - ShopMVC</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css">
</head>
<body>
<jsp:include page="/views/topbar.jsp" />

<main class="auth-page">
    <section class="auth-card" aria-labelledby="login-title">
        <div class="auth-header">
            <h1 id="login-title">Đăng nhập</h1>
            <p>Sử dụng tài khoản của bạn để tiếp tục.</p>
        </div>

        <c:if test="${param.registered == '1'}">
            <div class="alert alert-success">Tạo tài khoản thành công. Vui lòng đăng nhập.</div>
        </c:if>
        <c:if test="${param.requireAuth == '1'}">
            <div class="alert alert-warning">Vui lòng đăng nhập để truy cập trang này.</div>
        </c:if>
        <c:if test="${alert != null}">
            <div class="alert alert-danger"><c:out value="${alert}" /></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="post">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
            <div class="field-group">
                <label for="username">Tên đăng nhập</label>
                <input id="username" class="input-control" type="text" name="username" value="<c:out value='${rememberedUsername}' />" placeholder="Nhập tên đăng nhập" required autocomplete="username">
            </div>

            <div class="field-group">
                <label for="password">Mật khẩu</label>
                <input id="password" class="input-control" type="password" name="password" placeholder="Nhập mật khẩu" required autocomplete="current-password">
            </div>

            <label class="checkbox-row">
                <input type="checkbox" name="remember" ${remembered ? 'checked' : ''}>
                <span>Ghi nhớ đăng nhập</span>
            </label>

            <button class="button button-primary auth-submit" type="submit">Đăng nhập</button>
        </form>

        <p class="demo-note">Tài khoản mẫu: <strong>admin</strong> / <strong>123456</strong></p>

        <div class="auth-note">
            Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký</a>
        </div>
    </section>
</main>
</body>
</html>
