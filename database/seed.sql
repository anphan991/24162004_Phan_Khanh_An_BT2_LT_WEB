USE bt_lt_web;

-- Mật khẩu của tài khoản admin bên dưới là: 123456
-- Hash được tạo bằng PBKDF2-HMAC-SHA256, 120000 vòng lặp, salt riêng.
INSERT INTO users (email, username, fullname, password, roleid, createdDate)
VALUES (
    'admin@example.com',
    'admin',
    'Quản trị viên',
    'pbkdf2$120000$Ua7LbXovTKy6btPkYokEOg==$+20PWbrYZc7NNqWAAZbdFZEgpmngmwT08CGrPvw4iHk=',
    1,
    CURRENT_DATE
)
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT IGNORE INTO Category (cate_name, icons) VALUES
    ('Quần Áo Nam', 'default-men.svg'),
    ('Quần Áo Nữ', 'default-women.svg'),
    ('Giày', 'default-category.svg');
