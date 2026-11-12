<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chỉnh sửa danh mục - ShopMVC</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
</head>
<body class="admin-page">
<div class="admin-shell">
    <jsp:include page="/views/admin/sidebar.jsp" />

    <div class="admin-workspace">
        <header class="admin-header">
            <h1>Quản lý hệ thống</h1>
            <div class="admin-header-meta">
                <span>Đăng nhập: <strong><c:out value="${sessionScope.account.userName}" /></strong></span>
                <a class="admin-button admin-button-danger" href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
            </div>
        </header>

        <main class="admin-form-container">
            <section class="admin-form-card">
                <div class="admin-form-header">
                    <h1>Chỉnh sửa danh mục</h1>
                    <a class="admin-back" href="${pageContext.request.contextPath}/admin/category/list">Quay lại danh sách</a>
                </div>
                <div class="admin-form-body">
                    <c:if test="${not empty alert}">
                        <div class="admin-alert"><c:out value="${alert}" /></div>
                    </c:if>
                    <c:url value="/admin/category/edit" var="edit" />
                    <form action="${edit}" method="post" enctype="multipart/form-data">
                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                        <input type="hidden" name="id" value="<c:out value='${category.cateid}' />">

                        <div class="admin-field">
                            <label for="catename">Tên danh mục</label>
                            <input id="catename" type="text" value="<c:out value='${category.name}' />" name="catename" required>
                        </div>

                        <div class="admin-field">
                            <label for="icon">Ảnh đại diện</label>
                            <c:url value="/image" var="imgUrl">
                                <c:param name="fname" value="${category.icon}" />
                            </c:url>
                            <div class="admin-current-image">
                                <img id="previewImg" src="${imgUrl}" alt="<c:out value='${category.name}' />">
                                <div>
                                    <p>Chọn ảnh mới nếu muốn thay đổi. Bỏ trống để giữ ảnh hiện tại.</p>
                                    <input id="icon" type="file" name="icon" accept="image/*" onchange="previewEdit(this)">
                                </div>
                            </div>
                        </div>

                        <div class="admin-form-actions">
                            <button class="admin-button admin-button-primary" type="submit">Lưu thay đổi</button>
                            <button class="admin-button admin-button-secondary" type="reset">Khôi phục</button>
                            <a class="admin-button admin-button-secondary" href="${pageContext.request.contextPath}/admin/category/list">Hủy</a>
                        </div>
                    </form>
                </div>
            </section>
        </main>
    </div>
</div>

<script>
function previewEdit(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function (event) {
            document.getElementById('previewImg').src = event.target.result;
        };
        reader.readAsDataURL(input.files[0]);
    }
}
</script>
</body>
</html>
