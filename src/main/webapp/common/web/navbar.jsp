<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/taglib.jsp" %>
<ul class="navbar-nav me-auto mb-2 mb-lg-0 gap-1 ms-lg-3">
    <li class="nav-item">
        <a class="nav-link sitemesh-nav-link ${reqPath == '/index' || reqPath == '/' || reqPath == '/views/index.jsp' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/">Trang chủ</a>
    </li>
    <c:if test="${sessionScope.account != null}">
        <li class="nav-item">
            <a class="nav-link sitemesh-nav-link ${reqPath.startsWith('/video') || reqPath.startsWith('/views/video') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/video">Video</a>
        </li>
        <li class="nav-item">
            <a class="nav-link sitemesh-nav-link ${reqPath == '/home' || reqPath == '/views/home.jsp' ? 'active' : ''}"
               href="${pageContext.request.contextPath}/home">Danh mục</a>
        </li>
    </c:if>
    <c:if test="${sessionScope.account != null && (sessionScope.account.roleid == 1 || sessionScope.account.roleid == 2)}">
        <li class="nav-item">
            <a class="nav-link sitemesh-nav-link ${reqPath.startsWith('/admin') || reqPath.startsWith('/views/admin') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/admin/category/list">Quản trị</a>
        </li>
    </c:if>
</ul>
