<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>ShopMVC - Danh mục sản phẩm</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/site.css">
</head>
<body class="${empty sessionScope.account ? 'landing-page' : ''}">
<jsp:include page="/views/topbar.jsp" />

<section class="home-hero">
    <div class="home-hero-inner">
        <span class="eyebrow">Danh mục sản phẩm</span>
        <h1>Mọi danh mục cần thiết, ở đúng một nơi.</h1>
        <p>
            <c:choose>
                <c:when test="${empty sessionScope.account}">Đăng nhập để xem danh mục sản phẩm và tiếp tục làm việc.</c:when>
                <c:otherwise>Tra cứu danh mục nhanh chóng theo tài khoản của bạn.</c:otherwise>
            </c:choose>
        </p>

        <div class="hero-actions">
            <c:choose>
                <c:when test="${empty sessionScope.account}">
                    <a class="button button-primary" href="<c:url value='/login'/>">Đăng nhập</a>
                    <a class="button button-secondary" href="<c:url value='/register'/>">Tạo tài khoản</a>
                </c:when>
                <c:when test="${sessionScope.account.roleid == 1 || sessionScope.account.roleid == 2}">
                    <a class="button button-primary" href="<c:url value='/admin/category/list'/>">Mở trang quản trị</a>
                    <a class="button button-secondary" href="<c:url value='/home'/>">Danh mục</a>
                </c:when>
                <c:otherwise>
                    <a class="button button-primary" href="<c:url value='/home'/>">Mở danh mục</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</section>

<c:if test="${not empty sessionScope.account}">
    <main class="page-container catalog-section">
            <div class="section-heading">
                <div>
                    <h2>Danh mục sản phẩm</h2>
                    <p><c:out value="${cateList.size()}" /> danh mục đang được hiển thị</p>
                </div>
                <form class="search-form" action="${pageContext.request.contextPath}/index" method="get">
                    <input type="search" name="keyword" value="<c:out value='${keyword}' />" placeholder="Tìm danh mục" aria-label="Tìm danh mục">
                    <button class="button button-primary" type="submit">Tìm kiếm</button>
                </form>
            </div>

            <c:if test="${not empty dbError}">
                <div class="catalog-alert"><c:out value="${dbError}" /></div>
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
    </main>
</c:if>
</body>
</html>
