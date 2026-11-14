# BT2 Lập trình Web - Quản lý danh mục và tài khoản (Servlet/JSP + JPA/Hibernate)

Ứng dụng web Java theo mô hình MVC sử dụng Jakarta Servlet/JSP và **JPA / Hibernate ORM** kết nối MySQL, tập trung vào **CRUD Danh mục (`Category`)**, tìm kiếm có phân trang, upload ảnh biểu tượng, xác thực người dùng, remember-me token và phân quyền quản trị.

## Công nghệ sử dụng

- **Java**: 17 (`maven.compiler.release=17`)
- **Web**: Jakarta Servlet 6.0, Jakarta Server Pages (JSP) 3.1, Jakarta JSTL 3.0.1
- **ORM / Database**: Jakarta Persistence 3.2.0, Hibernate Core 7.4.6.Final, MySQL Connector/J 8.4.0
- **Validation**: Jakarta Validation 3.1.1, Hibernate Validator 8.0.5.Final, Jakarta EL 4.0.2
- **Testing**: JUnit Jupiter 5.10.2 (`junit-jupiter`)
- **Build**: Apache Maven (`war` packaging)

## Cấu trúc thư mục chính

```text
BT2_LT_Web/
├── database/
│   ├── schema.sql                  # Tạo DB bt_lt_web và các bảng users, Category, remember_tokens
│   └── seed.sql                    # Dữ liệu mẫu ban đầu
├── src/
│   ├── main/
│   │   ├── java/lt_web/
│   │   │   ├── controllers/        # AppContextListener, Auth/Home/Image controllers và Filters
│   │   │   │   └── admin/          # CategoryList/Add/Edit/Delete và UserAdd/Edit/Delete controllers
│   │   │   ├── entity/             # JPA Entities: Category, User, RememberToken
│   │   │   ├── exceptions/         # DataAccessException
│   │   │   ├── models/             # DTO/Form models: LoginModel, UserModel
│   │   │   ├── repository/         # JpaConfig, ICategoryRepository, IUserRepository, IRememberTokenRepository
│   │   │   │   └── impl/           # CategoryRepository, UserRepository, RememberTokenRepository
│   │   │   ├── services/           # AuthService, CategoryService, UserService, RememberMeService
│   │   │   │   └── impl/           # CategoryServiceImpl, UserServiceImpl
│   │   │   └── utils/              # Constant, CsrfToken, FileStorage, PasswordHasher, TokenUtil, ValidationUtil, AdminAccess
│   │   ├── resources/
│   │   │   ├── META-INF/persistence.xml   # Persistence unit "jpa-hibernate-mysql"
│   │   │   └── db.properties.example      # Mẫu cấu hình kết nối MySQL
│   │   └── webapp/
│   │       ├── assets/css/         # Stylesheets (bao gồm admin.css với phân trang)
│   │       ├── views/              # login.jsp, register.jsp, home.jsp và views/admin/*
│   │       └── WEB-INF/web.xml     # Filters, servlets, AppContextListener, session-config
│   └── test/java/lt_web/
│       ├── utils/                  # PasswordHasherTest, TokenUtilTest, ValidationUtilTest
│       ├── services/               # RememberMeServiceTest
│       ├── services/impl/          # CategoryServiceImplTest, UserServiceImplTest
│       └── repository/             # RememberTokenRepositoryIntegrationTest
└── pom.xml
```

## Kiến trúc JPA và các tính năng 

- **Cấu hình JPA tập trung**: `JpaConfig` khởi tạo singleton `EntityManagerFactory` từ persistence unit `jpa-hibernate-mysql` (`META-INF/persistence.xml`), tự nạp cấu hình DB runtime (file `db.properties`, JVM `-Dbt.lt.db.*`, hoặc biến môi trường `BT_LT_DB_*`), và được đóng gọn khi ứng dụng shutdown thông qua `AppContextListener`.
- **CRUD Danh mục (`Category`) bằng JPA/Hibernate**:
  - Entity `Category` ánh xạ bảng `Category` (`cateid`, `catename`, `icon`) với `@NamedQuery("Category.findAll", query = "SELECT c FROM Category c ORDER BY c.cateid ASC")`.
  - `CategoryRepository` sử dụng `EntityManager` và `EntityTransaction` cho các thao tác thêm (`create`/`insert`), sửa (`update`/`edit`), xóa (`delete`), truy vấn theo ID/tên, đếm bản ghi (`count`, `countByCategoryname`), tìm kiếm và phân trang (`findAll(page, pagesize)`, `findByCategoryname(catname, page, pagesize)`).
  - `CategoryListController` hỗ trợ tìm kiếm theo từ khóa và phân trang (5 danh mục/trang) tại `/admin/category/list`.
  - `CategoryAddController` và `CategoryEditController` validate dữ liệu trước khi lưu file ảnh, tự động dọn orphan file nếu validate/lưu DB thất bại, bắt `DataAccessException` để báo lỗi thân thiện trên form và giữ lại giá trị `catename` vừa nhập.
  - `CategoryServiceImpl` chỉ xóa ảnh custom cũ sau khi cập nhật DB thành công và dọn ảnh custom khi xóa danh mục (bỏ qua các icon mặc định `default-*`).
- Mã hóa mật khẩu (`PasswordHasher`), Remember-Me token (`RememberToken`, `RememberMeFilter`), chống CSRF (`CsrfFilter`, `CsrfToken`), kiểm tra quyền (`AuthorizationFilter`, `AdminAccess`) và Jakarta Bean Validation (`ValidationUtil`).

## Cài đặt và chạy

### 1. Chuẩn bị cơ sở dữ liệu MySQL

1. Chạy script [`database/schema.sql`](database/schema.sql) để tạo database `bt_lt_web` và các bảng `users`, `Category`, `remember_tokens`.
2. (Tùy chọn) Chạy [`database/seed.sql`](database/seed.sql) hoặc để `JpaConfig` tự khởi tạo dữ liệu mẫu ban đầu khi khởi động.

### 2. Cấu hình kết nối DB và thư mục upload

Tạo `src/main/resources/db.properties` từ [`src/main/resources/db.properties.example`](src/main/resources/db.properties.example), hoặc cấu hình qua biến môi trường / JVM properties:

- Database:
  - `bt.lt.db.url` / `BT_LT_DB_URL`
  - `bt.lt.db.user` / `BT_LT_DB_USER`
  - `bt.lt.db.password` / `BT_LT_DB_PASSWORD`
- Thư mục lưu ảnh upload (`Constant.uploadDirectory()`):
  - `bt.lt.upload.dir` / `BT_LT_UPLOAD_DIR` (mặc định: `<user.home>/BT2_LT_Web/uploads`)

### 3. Build và chạy

1. Build WAR với Maven:
   ```bash
   mvn clean package
   ```
2. Deploy `BT2_LT_Web` lên Apache Tomcat 10.1+ / Tomcat 11 (tương thích Jakarta Servlet 6.0).

## Kiểm thử

Chạy toàn bộ unit test (không cần MySQL):

```bash
mvn test
```

Chạy thêm integration test với MySQL (chẳng hạn `RememberTokenRepositoryIntegrationTest`):

```bash
BT_LT_RUN_DB_TESTS=true mvn test
```
