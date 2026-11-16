# BT Lập trình Web - Quản lý danh mục và tài khoản (Servlet/JSP + JPA/Hibernate)

Ứng dụng web Java theo mô hình MVC sử dụng Jakarta Servlet/JSP và **JPA / Hibernate ORM** kết nối MySQL, tập trung vào **CRUD Danh mục (`Category`)**, tìm kiếm có phân trang, upload ảnh biểu tượng, **xác thực & kích hoạt tài khoản bằng OTP qua email**, **đăng nhập với mật khẩu mã hóa (hash password)**, **đăng xuất**, **quên mật khẩu xác thực OTP qua mail**, remember-me token và phân quyền quản trị.

## Công nghệ sử dụng

- **Java**: 17 (`maven.compiler.release=17`)
- **Web**: Jakarta Servlet 6.0, Jakarta Server Pages (JSP) 3.1, Jakarta JSTL 3.0.1
- **ORM / Database**: Jakarta Persistence 3.2.0, Hibernate Core 7.4.6.Final, MySQL Connector/J 8.4.0
- **Validation**: Jakarta Validation 3.1.1, Hibernate Validator 8.0.5.Final, Jakarta EL 4.0.2
- **Email**: Jakarta Mail 2.0 (Angus Mail 2.0.3)
- **Testing**: JUnit Jupiter 5.10.2 (`junit-jupiter`)
- **Build**: Apache Maven (`war` packaging)

## Cấu trúc thư mục chính

```text
BT2_LT_Web/
├── database/
│   ├── schema.sql                  # Tạo DB bt_lt_web và các bảng users, Category, remember_tokens, otp_tokens
│   └── seed.sql                    # Dữ liệu mẫu ban đầu
├── src/
│   ├── main/
│   │   ├── java/lt_web/
│   │   │   ├── controllers/        # Login, Logout, Register, VerifyOtp, ResendOtp, ForgotPassword, ResetPassword, AppContextListener, Filters
│   │   │   │   └── admin/          # CategoryList/Add/Edit/Delete và UserAdd/Edit/Delete controllers
│   │   │   ├── entity/             # JPA Entities: Category, User, RememberToken, OtpToken
│   │   │   ├── exceptions/         # DataAccessException
│   │   │   ├── models/             # DTO/Form models: LoginModel, UserModel
│   │   │   ├── repository/         # JpaConfig, ICategoryRepository, IUserRepository, IRememberTokenRepository, IOtpRepository
│   │   │   │   └── impl/           # CategoryRepository, UserRepository, RememberTokenRepository, OtpRepository
│   │   │   ├── services/           # AuthService, CategoryService, UserService, RememberMeService, OtpService, EmailService
│   │   │   │   └── impl/           # CategoryServiceImpl, UserServiceImpl, OtpServiceImpl, EmailServiceImpl
│   │   │   └── utils/              # Constant, CsrfToken, FileStorage, PasswordHasher, TokenUtil, ValidationUtil, AdminAccess
│   │   ├── resources/
│   │   │   ├── META-INF/persistence.xml   # Persistence unit "jpa-hibernate-mysql"
│   │   │   └── db.properties.example      # Mẫu cấu hình kết nối MySQL & SMTP
│   │   └── webapp/
│   │       ├── assets/css/         # Stylesheets (site.css, admin.css với phân trang)
│   │       ├── views/              # login.jsp, register.jsp, verify-otp.jsp, forgot-password.jsp, reset-password.jsp, home.jsp và views/admin/*
│   │       └── WEB-INF/web.xml     # Filters, servlets, AppContextListener, session-config
│   └── test/java/lt_web/
│       ├── utils/                  # PasswordHasherTest, TokenUtilTest, ValidationUtilTest
│       ├── services/               # RememberMeServiceTest
│       ├── services/impl/          # CategoryServiceImplTest, UserServiceImplTest, OtpServiceImplTest
│       └── repository/             # RememberTokenRepositoryIntegrationTest
└── pom.xml
```

## Kiến trúc JPA và các tính năng chính

### 1. Kích hoạt tài khoản bằng mã OTP gửi qua email khi đăng ký
- Khi người dùng đăng ký tại `/register`, mật khẩu được hash an toàn bằng **PBKDF2WithHmacSHA256**, tài khoản được tạo trong MySQL với trạng thái `status = 0` (chờ kích hoạt).
- Hệ thống sinh mã OTP ngẫu nhiên 6 chữ số (`OtpService`), lưu vào bảng `otp_tokens` qua JPA entity `OtpToken` với thời hạn hiệu lực **5 phút**, đồng thời gửi email thông báo mã OTP qua `EmailService`.
- Người dùng được chuyển hướng đến `/verify-otp?email=...&type=activation` để nhập mã OTP:
  - Nếu nhập đúng mã và còn hạn: hệ thống cập nhật `status = 1` (đã kích hoạt) và chuyển sang trang đăng nhập `/login?activated=1`.
  - Có nút **Gửi lại mã OTP** (`/resend-otp`), tự động vô hiệu hóa mã cũ và gửi mã OTP mới.

### 2. Đăng nhập với mật khẩu được mã hóa (Hash Password) và Đăng xuất
- **Đăng nhập (`/login`)**:
  - Mật khẩu người dùng nhập vào được xác thực bằng `PasswordHasher.matches(rawPassword, storedHash)` (chuẩn PBKDF2 with HMAC-SHA256, 120.000 vòng lặp và salt ngẫu nhiên).
  - Tự động kiểm tra trạng thái kích hoạt tài khoản: nếu tài khoản có `status == 0`, hệ thống từ chối đăng nhập và hiển thị liên kết trực tiếp để người dùng nhập mã OTP kích hoạt.
  - Hỗ trợ tính năng **Ghi nhớ đăng nhập (Remember Me)** lưu token an toàn trong bảng `remember_tokens` qua cookie HttpOnly.
- **Đăng xuất (`/logout`)**:
  - Hỗ trợ cả phương thức GET và POST.
  - Hủy bỏ phiên làm việc (`HttpSession.invalidate()`), xóa bỏ token remember-me trong cơ sở dữ liệu và dọn sạch các cookie xác thực liên quan.

### 3. Quên mật khẩu gửi mã xác nhận OTP qua email
- Tại trang đăng nhập có liên kết **Quên mật khẩu?** dẫn đến `/forgot-password`.
- Người dùng nhập email đã đăng ký. Hệ thống kiểm tra tài khoản, sinh mã OTP 6 chữ số (loại `FORGOT_PASSWORD`, hiệu lực 5 phút) và gửi email qua `EmailService`.
- Người dùng nhập mã OTP tại `/verify-otp?type=forgot` hoặc trực tiếp tại `/reset-password`:
  - Sau khi mã OTP được xác thực thành công, người dùng nhập mật khẩu mới và xác nhận mật khẩu.
  - Mật khẩu mới được hash và lưu vào cơ sở dữ liệu qua JPA, đồng thời vô hiệu hóa phiên đăng nhập cũ và chuyển về `/login?resetSuccess=1`.

### 4. Quản lý danh mục (Category CRUD) bằng JPA
- Entity `Category` ánh xạ bảng `Category` (`cate_id`, `cate_name`, `icons`) với `@NamedQuery("Category.findAll", query = "SELECT c FROM Category c ORDER BY c.cateid ASC")`.
- `CategoryRepository` thực hiện đầy đủ các thao tác thêm, sửa, xóa, tìm kiếm theo tên và phân trang (`findAll(page, pagesize)`, `findByCategoryname(catname, page, pagesize)`).
- Validate dữ liệu, tự động dọn dẹp file upload mồ côi nếu validate hoặc thao tác cơ sở dữ liệu thất bại, bảo lưu giá trị nhập khi có lỗi.

## Cấu hình gửi Mail (SMTP / Dev Mode)

Trong file `src/main/resources/db.properties` (hoặc biến môi trường / JVM properties):

```properties
# Cấu hình SMTP gửi mail OTP (ví dụ Gmail SMTP)
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.username=your-email@gmail.com
mail.smtp.password=your-16-char-app-password
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.from.name=ShopMVC
```

> Nếu chưa cấu hình `mail.smtp.username` hoặc `mail.smtp.password`, hệ thống sẽ tự động chạy ở chế độ fallback: mã OTP được in trực tiếp ra console để có thể test chức năng mà không cần nhập App Password của Gmail.

## Cài đặt và chạy

### 1. Chuẩn bị cơ sở dữ liệu MySQL

1. Chạy script [`database/schema.sql`](database/schema.sql) để tạo database `bt_lt_web` và các bảng `users`, `Category`, `remember_tokens`, `otp_tokens`.
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

## Unit Tests

Chạy toàn bộ unit test (21 tests, không cần MySQL):

```bash
mvn test
```

Chạy thêm integration test với MySQL (chẳng hạn `RememberTokenRepositoryIntegrationTest`):

```bash
BT_LT_RUN_DB_TESTS=true mvn test
```
