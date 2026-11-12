<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="isEdit" value="${formMode == 'edit'}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${isEdit ? 'Chỉnh sửa tài khoản' : 'Thêm tài khoản'} - ShopMVC</title>
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
                    <h1>${isEdit ? 'Chỉnh sửa tài khoản' : 'Thêm tài khoản'}</h1>
                    <a class="admin-back" href="${pageContext.request.contextPath}/admin/category/list#users-section">Quay lại danh sách</a>
                </div>
                <div class="admin-form-body">
                    <c:if test="${not empty alert}">
                        <div class="admin-alert"><c:out value="${alert}" /></div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}${isEdit ? '/admin/user/edit' : '/admin/user/add'}" method="post">
                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                        <c:if test="${isEdit}">
                            <input type="hidden" name="id" value="${user.id}">
                        </c:if>

                        <div class="form-grid-admin">
                            <div class="admin-field">
                                <label for="username">Tên đăng nhập</label>
                                <input id="username" type="text" name="username" value="<c:out value='${user.userName}' />" required ${isEdit ? 'readonly' : ''}>
                            </div>
                            <div class="admin-field">
                                <label for="fullname">Họ và tên</label>
                                <input id="fullname" type="text" name="fullname" value="<c:out value='${user.fullName}' />" required>
                            </div>
                            <div class="admin-field">
                                <label for="email">Email</label>
                                <input id="email" type="email" name="email" value="<c:out value='${user.email}' />">
                            </div>
                            <div class="admin-field">
                                <label for="phone">Số điện thoại</label>
                                <input id="phone" type="text" name="phone" value="<c:out value='${user.phone}' />">
                            </div>
                            <div class="admin-field">
                                <label for="password">Mật khẩu${isEdit ? ' mới' : ''}</label>
                                <input id="password" type="password" name="password" ${isEdit ? '' : 'required'} autocomplete="new-password">
                                <p class="admin-field-hint">${isEdit ? 'Để trống nếu không muốn đổi mật khẩu.' : 'Mật khẩu sẽ được mã hóa trước khi lưu.'}</p>
                            </div>
                            <div class="admin-field">
                                <label for="roleid">Vai trò</label>
                                <select id="roleid" name="roleid">
                                    <option value="1" ${user.roleid == 1 ? 'selected' : ''}>Admin</option>
                                    <option value="2" ${user.roleid == 2 ? 'selected' : ''}>Manager</option>
                                    <option value="5" ${user.roleid == 5 || user.roleid == 0 ? 'selected' : ''}>Member</option>
                                </select>
                            </div>
                        </div>

                        <div class="admin-form-actions">
                            <button class="admin-button admin-button-primary" type="submit">${isEdit ? 'Lưu thay đổi' : 'Thêm tài khoản'}</button>
                            <a class="admin-button admin-button-secondary" href="${pageContext.request.contextPath}/admin/category/list#users-section">Hủy</a>
                        </div>
                    </form>
                </div>
            </section>
        </main>
    </div>
</div>
</body>
</html>
