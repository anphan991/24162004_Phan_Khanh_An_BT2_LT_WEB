<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<aside class="admin-sidebar">
    <a class="admin-brand" href="${pageContext.request.contextPath}/admin/category/list"><span>ShopMVC Admin</span></a>

    <div class="admin-profile">
        <div class="admin-profile-name"><c:out value="${not empty sessionScope.account ? sessionScope.account.fullName : 'Admin'}" /></div>
        <div class="admin-profile-role">
            <c:choose>
                <c:when test="${sessionScope.account.roleid == 1}">Quản trị viên</c:when>
                <c:otherwise>Quản lý</c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="admin-nav-label">Quản lý</div>
    <a class="admin-link ${pageContext.request.servletPath == '/admin/category/list' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/category/list">Danh mục sản phẩm</a>
    <a class="admin-link ${pageContext.request.servletPath == '/admin/category/add' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/category/add">Thêm danh mục</a>
    <a class="admin-link" href="${pageContext.request.contextPath}/admin/category/list#users-section">Tài khoản</a>

    <div class="admin-nav-label admin-nav-spacer">Truy cập nhanh</div>
    <a class="admin-link" href="${pageContext.request.contextPath}/home">Danh mục</a>
    <a class="admin-link" href="${pageContext.request.contextPath}/">Trang chủ</a>
</aside>
