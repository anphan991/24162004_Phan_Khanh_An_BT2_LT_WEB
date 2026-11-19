<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/taglib.jsp" %>
<div class="d-flex align-items-center gap-2">
    <c:choose>
        <c:when test="${sessionScope.account == null}">
            <a class="btn btn-outline-secondary btn-sm fw-semibold px-3" href="${pageContext.request.contextPath}/login">Đăng nhập</a>
            <a class="btn btn-primary btn-sm fw-semibold px-3" href="${pageContext.request.contextPath}/register">Đăng ký</a>
        </c:when>
        <c:otherwise>
            <div class="d-flex align-items-center gap-2 me-2">
                <span class="sitemesh-avatar"><c:out value="${sessionScope.account.userName.substring(0, 1)}" /></span>
                <span class="fw-semibold text-dark small"><c:out value="${sessionScope.account.fullName}" /></span>
                <span class="badge bg-light text-secondary border">
                    <c:choose>
                        <c:when test="${sessionScope.account.roleid == 1}">Admin</c:when>
                        <c:when test="${sessionScope.account.roleid == 2}">Manager</c:when>
                        <c:otherwise>Member</c:otherwise>
                    </c:choose>
                </span>
            </div>
            <a class="btn btn-outline-danger btn-sm fw-semibold" href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
        </c:otherwise>
    </c:choose>
</div>
