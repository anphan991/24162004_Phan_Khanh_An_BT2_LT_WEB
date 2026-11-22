# BT Lập trình Web - ShopMVC (Servlet/JSP + JPA/Hibernate + SiteMesh 3 + Cloudinary)

Ứng dụng web Java theo mô hình MVC sử dụng Jakarta Servlet/JSP, **JPA / Hibernate ORM** kết nối MySQL và **SiteMesh Decorator 3** với giao diện **Bootstrap 5**, hỗ trợ **CRUD Danh mục (`Category`) và Video (`Videos`)**, **Quản lý Hồ sơ cá nhân (`/profile`)** (cập nhật thông tin, **xác nhận OTP qua email mới**, upload ảnh **Multipart lên Cloudinary**), **Validation Form** (Client-side + Server-side), đăng ký/kích hoạt OTP, đăng nhập hash password, quên mật khẩu OTP và phân quyền quản trị.

## Công nghệ sử dụng

- **Java**: 17 (`maven.compiler.release=17`)
- **Web**: Jakarta Servlet 6.0, Jakarta Server Pages (JSP) 3.1, Jakarta JSTL 3.0.1
- **Layout / UI**: SiteMesh Decorator 3 (`org.sitemesh:sitemesh:3.2.2`), Bootstrap 5.3.3, Bootstrap Icons 1.11.3
- **ORM / Database**: Jakarta Persistence 3.2.0, Hibernate Core 7.4.6.Final, MySQL Connector/J 8.4.0
- **Validation**: Jakarta Validation 3.1.1, Hibernate Validator 8.0.5.Final, Jakarta EL 4.0.2 + Bootstrap validation (`window.ShopValidation`)
- **Email và Cloud Upload**: Jakarta Mail 2.0 (Angus Mail 2.0.3), Cloudinary REST API (`multipart/form-data`, `java.net.http.HttpClient`)
- **Utilities**: Project Lombok 1.18.48 (`provided`)
- **Testing**: JUnit Jupiter 5.10.2 (`junit-jupiter`)
- **Build**: Apache Maven (`war` packaging)

## Cấu trúc thư mục chính

```text
BT2_LT_Web/
├── database/
│   ├── schema.sql                  # Tạo DB bt_lt_web và các bảng users, Category, remember_tokens, otp_tokens, Videos
│   └── seed.sql                    # Dữ liệu mẫu ban đầu
├── src/
│   ├── main/
│   │   ├── java/lt_web/
│   │   │   ├── controllers/        # Home, Login, Logout, Register, VerifyOtp, ResendOtp, ForgotPassword, ResetPassword, Profile, Video, DownloadImage, DownloadVideo, Filters
│   │   │   │   └── admin/          # CategoryList/Add/Edit/Delete và UserAdd/Edit/Delete controllers
│   │   │   ├── entity/             # JPA Entities: Category, User, RememberToken, OtpToken, Video (dùng Lombok)
│   │   │   ├── exceptions/         # DataAccessException
│   │   │   ├── models/             # DTO/Form models: LoginModel, UserModel
│   │   │   ├── repository/         # JpaConfig, ICategoryRepository, IUserRepository, IRememberTokenRepository, IOtpRepository, IVideoRepository
│   │   │   │   └── impl/           # CategoryRepository, UserRepository, RememberTokenRepository, OtpRepository, VideoRepository
│   │   │   ├── services/           # CategoryService, UserService, RememberMeService, OtpService, EmailService, VideoService
│   │   │   │   └── impl/           # CategoryServiceImpl, UserServiceImpl, OtpServiceImpl, EmailServiceImpl, VideoServiceImpl
│   │   │   └── utils/              # CloudinaryService, Constant, CsrfToken, FileStorage, PasswordHasher, TokenUtil, ValidationUtil, AdminAccess
│   │   ├── resources/
│   │   │   ├── META-INF/persistence.xml   # Persistence unit "jpa-hibernate-mysql"
│   │   │   └── db.properties.example      # Mẫu cấu hình kết nối MySQL, SMTP và Cloudinary
│   │   └── webapp/
│   │       ├── assets/css/         # site.css, admin.css
│   │       ├── common/             # info.jsp, taglib.jsp, common/web/* (header, navbar, topbar, left, footer), common/admin/* (header, left, footer)
│   │       ├── views/              # index.jsp, home.jsp, profile.jsp, login.jsp, register.jsp, verify-otp.jsp, forgot-password.jsp, reset-password.jsp, video-*.jsp, views/admin/*
│   │       └── WEB-INF/
│   │           ├── decorators/     # Layout SiteMesh 3 nền Bootstrap 5 (web.jsp, admin.jsp, login.jsp)
│   │           ├── sitemesh3.xml   # Mapping layout và exclude /assets/*, /api*, /image, /video-file
│   │           └── web.xml         # Filters (RememberMe, Csrf, Authorization, ConfigurableSiteMeshFilter), servlets, session-config
│   └── test/java/lt_web/
│       ├── utils/                  # CloudinaryServiceTest, PasswordHasherTest, TokenUtilTest, ValidationUtilTest
│       ├── services/               # RememberMeServiceTest
│       ├── services/impl/          # CategoryServiceImplTest, UserServiceImplTest, OtpServiceImplTest, VideoServiceImplTest
│       └── repository/             # RememberTokenRepositoryIntegrationTest
└── pom.xml
```

## Kiến trúc và các tính năng chính

### 1. SiteMesh Decorator 3 và Giao diện Bootstrap 5
- Đăng ký `org.sitemesh.config.ConfigurableSiteMeshFilter` ánh xạ `/*` trong `WEB-INF/web.xml` và cấu hình quy tắc trong `WEB-INF/sitemesh3.xml`.
- Tổ chức cấu trúc layout chuẩn theo `common/` (dùng chung `taglib.jsp`, `info.jsp`, `header.jsp`, `navbar.jsp`, `topbar.jsp`, `left.jsp`, `footer.jsp`), `WEB-INF/decorators/` (`web.jsp`, `admin.jsp`, `login.jsp`) và `views/` (chỉ chứa nội dung trang).
- Tất cả các trang dùng chung hệ giao diện Bootstrap 5.3.3, Bootstrap Icons, font `Plus Jakarta Sans` và CSS đồng bộ.

### 2. Validation cho toàn bộ chức năng có Form
- **Server-side**: Kết hợp Jakarta Bean Validation trên Entity với các helper trong `ValidationUtil` (`fieldErrors`, `isValidUsername`, `isValidEmail`, `isValidPhone`, `isValidPassword`, `isValidOtp`, `isValidExternalUrl`), trả về `errors` theo từng trường và giữ nguyên dữ liệu người dùng đã nhập khi có lỗi.
- **Client-side**: Helper `window.ShopValidation` trong `common/info.jsp` kiểm tra và hiển thị lỗi Bootstrap `is-invalid` / `.invalid-feedback` ngay trên form trước khi submit.
- Áp dụng đầy đủ cho tất cả các form:
  - **Xác thực**: Đăng nhập (`/login`), Đăng ký (`/register`), Quên mật khẩu (`/forgot-password`), Xác nhận OTP (`/verify-otp`), Đặt lại mật khẩu (`/reset-password`).
  - **Hồ sơ cá nhân**: Cập nhật thông tin (`/profile`) và Xác nhận OTP đổi email mới.
  - **Thêm / Sửa dữ liệu**: Thêm và Sửa Danh mục (`/admin/category/add`, `/admin/category/edit`), Thêm và Sửa Tài khoản (`/admin/user/add`, `/admin/user/edit`), Thêm và Sửa Video (`/video/add`, `/video/edit`).

### 3. Hồ sơ cá nhân (`/profile`) và Upload Multipart lên Cloudinary
- Cho phép người dùng xem và cập nhật **`fullname`**, **`phone`**, **`email`**, và **`images`** (ảnh đại diện).
- **Xác thực OTP khi đổi email**: Nếu nhập địa chỉ email mới khác email hiện tại, hệ thống kiểm tra trùng lặp với các tài khoản khác, giữ nguyên email cũ cho đến khi sinh và gửi mã OTP 6 chữ số (loại `EMAIL_CHANGE`, hiệu lực 5 phút) tới **email mới**. Sau khi người dùng nhập đúng OTP (hoặc bấm gửi lại / hủy đổi email), email mới mới được lưu vào MySQL và cập nhật session.
- **Upload Multipart lên Cloudinary (`CloudinaryService`)**: Gửi `multipart/form-data` trực tiếp tới Cloudinary Upload API (`/image/upload` cho avatar, ảnh danh mục `shopmvc/categories`, poster video `shopmvc/posters`; `/video/upload` cho video `shopmvc/videos`) hỗ trợ cả Signed (SHA-1) và Unsigned Upload Preset. Nếu chưa cấu hình Cloudinary, hệ thống tự fallback sang lưu cục bộ qua `FileStorage`.

### 4. Đăng ký, Đăng nhập, Quên mật khẩu và Phân quyền
- Mật khẩu hash bằng **PBKDF2WithHmacSHA256** (120.000 vòng lặp, salt ngẫu nhiên).
- Đăng ký tài khoản (`status = 0`) yêu cầu xác nhận OTP gửi email (hiệu lực 5 phút) để kích hoạt (`status = 1`).
- Quên mật khẩu gửi OTP (`FORGOT_PASSWORD`) để đặt lại mật khẩu mới.
- Remember-me token (bảng `remember_tokens`, HttpOnly cookie) và bảo vệ CSRF cho mọi request ghi dữ liệu (`CsrfFilter`).

### 5. Quản lý Danh mục (`Category`) và Thư viện Video (`Videos`)
- CRUD Danh mục có tìm kiếm, phân trang và upload ảnh lên Cloudinary (hoặc local).
- Thư viện Video (6 video/trang) hỗ trợ tìm kiếm, lọc theo **Video của tôi / Tất cả video**, tải tệp video trực tiếp lên Cloudinary/local hoặc gắn link YouTube/MP4, đếm lượt xem và quản lý theo quyền (Admin/Manager hoặc chủ sở hữu video).

## Cấu hình MySQL, SMTP và Cloudinary

Tạo `src/main/resources/db.properties` từ [`src/main/resources/db.properties.example`](src/main/resources/db.properties.example), hoặc cấu hình qua biến môi trường / JVM properties:

- **MySQL**:
  - `db.url` (`bt.lt.db.url` / `BT_LT_DB_URL`)
  - `db.user` (`bt.lt.db.user` / `BT_LT_DB_USER`)
  - `db.password` (`bt.lt.db.password` / `BT_LT_DB_PASSWORD`)
- **Email SMTP** *(nếu để trống username/password, mã OTP sẽ được ghi trực tiếp ra console để tiện test offline)*:
  - `mail.smtp.host`, `mail.smtp.port`, `mail.smtp.username`, `mail.smtp.password`, `mail.from.name`
- **Cloudinary** *(hoặc `CLOUDINARY_URL` / `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`, `CLOUDINARY_UPLOAD_PRESET`)*:
  ```properties
  cloudinary.cloud_name=your_cloud_name
  cloudinary.api_key=your_api_key
  cloudinary.api_secret=your_api_secret
  cloudinary.upload_preset=
  cloudinary.folder=shopmvc/avatars
  ```
- **Thư mục upload fallback cục bộ**:
  - `bt.lt.upload.dir` / `BT_LT_UPLOAD_DIR` (mặc định: `<user.home>/BT2_LT_Web/uploads`)

## Cài đặt và chạy

1. Chạy [`database/schema.sql`](database/schema.sql) và (tùy chọn) [`database/seed.sql`](database/seed.sql) trong MySQL.
2. Build WAR với Maven hoặc Eclipse (`Maven -> Update Project`):
   ```powershell
   mvn clean package
   ```
3. Deploy `BT2_LT_Web` lên Apache Tomcat **10.1+** (Jakarta Servlet 6.0) và truy cập `http://localhost:8080/BT2_LT_Web/`.

## Unit Tests

Chạy toàn bộ unit test (34 unit tests, không cần MySQL; 1 integration test DB sẽ tự skip nếu chưa bật cờ):

```powershell
mvn test
```

Chạy thêm integration test với MySQL (`RememberTokenRepositoryIntegrationTest`):

```powershell
$env:BT_LT_RUN_DB_TESTS='true'; mvn test
```
