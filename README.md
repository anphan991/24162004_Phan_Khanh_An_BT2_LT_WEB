# BT2_LT_Web – Servlet, JSP/JSTL, JDBC, MVC ba tầng

Project BT2 được tạo từ BT_LT_Web và đã được đổi identity riêng để có thể chạy song song:

- Maven artifact: `BT2_LT_Web`
- WAR: `BT2_LT_Web.war`
- Tomcat context: `http://localhost:8080/BT2_LT_Web/`
- JDK: 17

## Chức năng

- Login bằng Session tại `/login`.
- Ghi nhớ đăng nhập bằng remember-token Cookie HttpOnly/SameSite; token chỉ lưu dưới dạng SHA-256 trong database và tự khôi phục Session trong 30 ngày.
- Register, logout và điều hướng theo vai trò.
- Member xem/tìm kiếm danh mục tại `/home`.
- Admin/Manager truy cập dashboard; Admin có thể CRUD Category và tài khoản.
- Upload ảnh Category với thư mục cấu hình được.
- PBKDF2-HMAC-SHA256 cho mật khẩu.
- CSRF token cho các request POST/PUT/PATCH/DELETE.
- Xóa Category có trang xác nhận và chỉ thực hiện bằng POST.

## Công nghệ

- JDK 17, Maven 3.9+
- Apache Tomcat 10.1+
- Jakarta Servlet 6.0, JSP 3.1, JSTL 3.0
- MySQL 8.0+
- MySQL Connector/J 8.4.0
- Jakarta Persistence 3.2 và Hibernate ORM 7.4.6.Final
- Hibernate Validator 8.0.5.Final, Jakarta Validation 3.1.1 và Jakarta EL 4.0.2

## Hibernate/JPA và Validation

Project đã tích hợp một persistence unit cho MySQL tại:

```text
src/main/resources/META-INF/persistence.xml
```

Persistence unit có tên `jpa-hibernate-mysql`, dùng entity `lt_web.models.Category`,
đúng chuẩn Jakarta Persistence và Hibernate ORM. Cấu hình kết nối thật được
ghi đè tại runtime từ `db.properties` hoặc biến môi trường, vì vậy mật khẩu
MySQL không cần ghi vào `persistence.xml` và không nên commit lên Git.

Ví dụ mở EntityManager:

```java
try (EntityManager entityManager = JpaConfig.getEntityManager()) {
    EntityTransaction transaction = entityManager.getTransaction();
    transaction.begin();
    Category category = entityManager.find(Category.class, categoryId);
    transaction.commit();
}
```

Các màn hình CRUD hiện tại vẫn dùng DAO/JDBC để giữ nguyên luồng bài Servlet
đang chạy ổn định; Hibernate/JPA là lớp persistence đã được tích hợp sẵn để
tiếp tục mở rộng hoặc chuyển dần từng DAO khi cần. Bean `Category` và `User`
đã có annotation validation, còn `ValidationUtil` dùng Hibernate Validator
để kiểm tra dữ liệu trước khi đăng ký và thêm/sửa danh mục.

## Cấu hình MySQL

BT2 hiện dùng chung database `bt_lt_web` với BT_LT_Web để chạy ngay trên dữ liệu hiện có.
Nếu muốn BT2 độc lập hoàn toàn, hãy đổi tên database trong `DBConnection.java`,
`db.properties` và hai file SQL trước khi chạy.

MySQL Server phải đang chạy; không cần mở MySQL Workbench.

Copy cấu hình mẫu:

```powershell
Copy-Item src/main/resources/db.properties.example src/main/resources/db.properties
```

Điền mật khẩu MySQL trong `src/main/resources/db.properties`, hoặc dùng biến môi trường:

```powershell
$env:BT_LT_DB_URL = 'jdbc:mysql://127.0.0.1:3306/bt_lt_web'
$env:BT_LT_DB_USER = 'root'
$env:BT_LT_DB_PASSWORD = 'MAT_KHAU_MYSQL_CUA_BAN'
```

Schema và dữ liệu mẫu nằm tại:

```text
database/schema.sql
database/seed.sql
```

Có thể chạy bằng MySQL CLI:

```powershell
mysql -u root -p < database/schema.sql
mysql -u root -p bt_lt_web < database/seed.sql
```

## Upload ảnh

Mặc định ảnh được lưu tại:

```text
%USERPROFILE%\BT2_LT_Web\uploads
```

Có thể đổi bằng `BT_LT_UPLOAD_DIR` hoặc JVM property `-Dbt.lt.upload.dir=...`.

## Build và chạy

```powershell
mvn clean test
mvn clean package
```

WAR tạo tại `target/BT2_LT_Web.war`. Copy WAR vào `TOMCAT_HOME/webapps`, khởi động Tomcat và mở:

```text
http://localhost:8080/BT2_LT_Web/
```

### Chạy bằng Eclipse/Spring Tools

1. Import thư mục `BT2_LT_Web` bằng `File → Import → Maven → Existing Maven Projects`.
2. Chọn JDK 17 và Tomcat 10.1.
3. Chạy `Maven → Update Project`.
4. Chọn `Project → Clean`.
5. Chọn `Run As → Run on Server`.

## Tài khoản mẫu

Sau khi chạy `database/seed.sql`:

| Tài khoản | Mật khẩu | Vai trò |
| --- | --- | --- |
| `admin` | `123456` | Admin |

Không commit `db.properties` hoặc mật khẩu thật lên GitHub.

## Kiểm tra

Unit test mặc định:

```powershell
mvn test
```

DAO integration test với MySQL chỉ chạy khi bật:

```powershell
$env:BT_LT_RUN_DB_TESTS = 'true'
mvn test
```

Checklist nhanh:

1. Admin đăng nhập và truy cập `/admin/category/list`.
2. Thêm, sửa, tìm kiếm và xóa Category.
3. Kiểm tra xóa Category phải qua trang xác nhận.
4. Thêm, sửa, xóa tài khoản trên dashboard.
5. Đăng xuất rồi truy cập lại với Cookie ghi nhớ để kiểm tra Session tự khôi phục.
6. Đăng ký tài khoản Member và kiểm tra phân quyền.

## Cấu trúc

```text
src/main/java/lt_web/
  controllers/       Servlet, AuthorizationFilter, CsrfFilter, RememberMeFilter
  controllers/admin/ Controller CRUD Category và tài khoản
  dao/               Interface và JDBC implementation
  models/            User, Category
  services/          DBConnection và nghiệp vụ
  utils/             Hash password, token, CSRF, upload, Bean Validation

src/main/resources/  Cấu hình DB mẫu và META-INF/persistence.xml
src/main/webapp/     JSP, CSS, WEB-INF/web.xml
src/test/java/       Unit test và DAO integration test
database/            schema.sql và seed.sql
```

Các thư mục build `target`, IDE metadata và cấu hình DB local đã được thêm vào `.gitignore`.
