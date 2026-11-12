<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<header class="site-topbar">
    <div class="topbar-inner">
        <a class="brand-link" href="${pageContext.request.contextPath}/">
            <span class="brand-mark" aria-hidden="true"></span>
            <span>ShopMVC</span>
        </a>

        <nav class="topbar-nav" aria-label="Menu chính">
            <a href="${pageContext.request.contextPath}/">Trang chủ</a>
            <c:if test="${sessionScope.account != null}">
                <a href="${pageContext.request.contextPath}/home">Danh mục</a>
            </c:if>
            <c:if test="${sessionScope.account != null && (sessionScope.account.roleid == 1 || sessionScope.account.roleid == 2)}">
                <a href="${pageContext.request.contextPath}/admin/category/list">Quản trị</a>
            </c:if>
        </nav>

        <div class="topbar-actions">
            <c:choose>
                <c:when test="${sessionScope.account == null}">
                    <a class="button button-quiet" href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                    <a class="button button-primary" href="${pageContext.request.contextPath}/register">Đăng ký</a>
                </c:when>
                <c:otherwise>
                    <div class="user-summary">
                        <span class="avatar"><c:out value="${sessionScope.account.userName.substring(0, 1)}" /></span>
                        <span><c:out value="${sessionScope.account.fullName}" /></span>
                        <span class="role-label">
                            <c:choose>
                                <c:when test="${sessionScope.account.roleid == 1}">Admin</c:when>
                                <c:when test="${sessionScope.account.roleid == 2}">Manager</c:when>
                                <c:otherwise>Member</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <a class="button button-quiet" href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>
