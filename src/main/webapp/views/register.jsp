<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng ký - ShopMVC</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css">
</head>
<body>
<jsp:include page="/views/topbar.jsp" />

<main class="auth-page">
    <section class="auth-card register-card" aria-labelledby="register-title">
        <div class="auth-header">
            <h1 id="register-title">Tạo tài khoản</h1>
            <p>Điền thông tin cơ bản để trở thành thành viên.</p>
        </div>

        <c:if test="${alert != null}">
            <div class="alert alert-danger"><c:out value="${alert}" /></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/register" method="post">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
            <div class="form-grid">
                <div class="field-group">
                    <label for="username">Tên đăng nhập</label>
                    <input id="username" class="input-control" type="text" name="username" value="<c:out value='${username}' />" placeholder="Ví dụ: nguyenvana" required autocomplete="username">
                </div>
                <div class="field-group">
                    <label for="fullname">Họ và tên</label>
                    <input id="fullname" class="input-control" type="text" name="fullname" value="<c:out value='${fullname}' />" placeholder="Nguyễn Văn A" required autocomplete="name">
                </div>
                <div class="field-group">
                    <label for="email">Email (nhận mã OTP)</label>
                    <input id="email" class="input-control" type="email" name="email" value="<c:out value='${email}' />" placeholder="email@domain.com" required autocomplete="email">
                </div>
                <div class="field-group">
                    <label for="phone">Số điện thoại</label>
                    <input id="phone" class="input-control" type="text" name="phone" value="<c:out value='${phone}' />" placeholder="0901234567" required autocomplete="tel">
                </div>
                <div class="field-group">
                    <label for="password">Mật khẩu</label>
                    <input id="password" class="input-control" type="password" name="password" placeholder="Tối thiểu 3 ký tự" required autocomplete="new-password">
                </div>
                <div class="field-group">
                    <label for="repassword">Nhập lại mật khẩu</label>
                    <input id="repassword" class="input-control" type="password" name="repassword" placeholder="Xác nhận mật khẩu" required autocomplete="new-password">
                </div>
            </div>

            <p class="form-note">Sau khi bấm Tạo tài khoản, hệ thống sẽ gửi một mã OTP 6 chữ số tới email của bạn để kích hoạt tài khoản.</p>

            <button class="button button-primary auth-submit" type="submit">Tạo tài khoản</button>
        </form>

        <div class="auth-note">
            Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
        </div>
    </section>
</main>
</body>
</html>
