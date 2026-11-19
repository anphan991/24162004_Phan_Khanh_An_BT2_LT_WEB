<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/taglib.jsp" %>
<c:set var="reqPath" value="${pageContext.request.servletPath}" />
<nav class="navbar navbar-expand-lg navbar-light sitemesh-navbar sticky-top">
    <div class="container-xl">
        <a class="navbar-brand d-flex align-items-center gap-2 fw-bold text-dark" href="${pageContext.request.contextPath}/">
            <span class="sitemesh-brand-mark" aria-hidden="true"></span>
            <span>ShopMVC</span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#shopNavbar"
                aria-controls="shopNavbar" aria-expanded="false" aria-label="Mở menu">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="shopNavbar">
            <%@ include file="/common/web/navbar.jsp" %>
            <%@ include file="/common/web/topbar.jsp" %>
        </div>
    </div>
</nav>
