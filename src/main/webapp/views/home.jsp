<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh mục - ShopMVC</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css">
</head>
<body>
<jsp:include page="/views/topbar.jsp" />

<main class="page-container member-main">
    <c:if test="${param.forbidden == '1'}">
        <div class="member-alert">Tài khoản thành viên không có quyền truy cập khu vực quản trị.</div>
    </c:if>

    <section class="member-intro" aria-labelledby="member-title">
        <div class="member-intro-content">
            <div class="avatar member-avatar">
                <c:out value="${not empty sessionScope.account ? sessionScope.account.userName.substring(0, 1) : 'U'}" />
            </div>
            <div>
                <h1 id="member-title">Xin chào, <c:out value="${not empty sessionScope.account ? sessionScope.account.fullName : sessionScope.username}" /></h1>
                <div class="member-meta">
                    <span><strong>Tài khoản:</strong> <c:out value="${sessionScope.account.userName}" /></span>
                    <span>
                        <strong>Vai trò:</strong>
                        <c:choose>
                            <c:when test="${sessionScope.account.roleid == 1}">Quản trị viên</c:when>
                            <c:when test="${sessionScope.account.roleid == 2}">Quản lý</c:when>
                            <c:otherwise>Thành viên</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
        </div>

        <c:if test="${sessionScope.account.roleid == 1 || sessionScope.account.roleid == 2}">
            <a class="button button-primary" href="${pageContext.request.contextPath}/admin/category/list">Mở trang quản trị</a>
        </c:if>
    </section>

    <section aria-labelledby="catalog-title">
        <div class="section-heading member-catalog-heading">
            <div>
                <h2 id="catalog-title">Danh mục sản phẩm</h2>
                <p><c:out value="${cateList.size()}" /> danh mục đang được hiển thị</p>
            </div>
            <form class="search-form" action="${pageContext.request.contextPath}/home" method="get">
                <input type="search" name="keyword" value="<c:out value='${keyword}' />" placeholder="Tìm danh mục" aria-label="Tìm danh mục">
                <button class="button button-primary" type="submit">Tìm kiếm</button>
            </form>
        </div>

        <c:if test="${not empty dbError}">
            <div class="member-alert"><c:out value="${dbError}" /></div>
        </c:if>

        <c:choose>
            <c:when test="${empty cateList}">
                <div class="empty-state">Chưa có danh mục phù hợp với tìm kiếm của bạn.</div>
            </c:when>
            <c:otherwise>
                <div class="catalog-grid">
                    <c:forEach items="${cateList}" var="cate">
                        <c:url value="/image" var="imgUrl">
                            <c:param name="fname" value="${cate.icon}" />
                        </c:url>
                        <article class="catalog-card">
                            <div class="catalog-image">
                                <img src="${imgUrl}" alt="<c:out value='${cate.catename}' />">
                            </div>
                            <div class="catalog-card-body">
                                <h3><c:out value="${cate.catename}" /></h3>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
</body>
</html>
