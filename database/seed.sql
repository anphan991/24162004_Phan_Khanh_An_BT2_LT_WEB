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

-- Dữ liệu mẫu 12 Videos gắn với tài khoản admin (quan hệ 1-N User - Videos)
INSERT INTO Videos (title, poster, video_path, description, views, active, created_at, user_id)
SELECT v.title, 'default-video-poster.svg', 'default-video.mp4', v.description, v.views, 1, v.created_at, u.id
FROM users u
CROSS JOIN (
    SELECT 'Giới thiệu bộ sưu tập Thu Đông 2026' AS title, 'Tổng hợp các mẫu áo khoác và áo len nổi bật cho mùa Thu Đông.' AS description, 128 AS views, DATE_SUB(NOW(), INTERVAL 12 HOUR) AS created_at
    UNION ALL SELECT 'Hướng dẫn chọn size áo sơ mi nam chuẩn dáng', 'Mẹo đo vòng ngực, chiều dài vai để chọn áo vừa vặn nhất.', 95, DATE_SUB(NOW(), INTERVAL 11 HOUR)
    UNION ALL SELECT 'Top 5 đôi giày sneaker dễ phối đồ nhất', 'Những mẫu giày basic phù hợp đi học, đi làm và dạo phố.', 210, DATE_SUB(NOW(), INTERVAL 10 HOUR)
    UNION ALL SELECT 'Cách bảo quản đồ jean luôn bền màu', 'Lưu ý khi giặt và phơi giúp quần jean không bị phai hay co rút.', 74, DATE_SUB(NOW(), INTERVAL 9 HOUR)
    UNION ALL SELECT 'Phối đồ công sở thanh lịch cho nữ', 'Gợi ý set blazer, chân váy và áo kiểu nhẹ nhàng nơi công sở.', 156, DATE_SUB(NOW(), INTERVAL 8 HOUR)
    UNION ALL SELECT 'Review chi tiết áo khoác dù chống nước', 'Kiểm tra chất liệu vải, đường may và độ thoáng khí thực tế.', 182, DATE_SUB(NOW(), INTERVAL 7 HOUR)
    UNION ALL SELECT 'Xu hướng phụ kiện tối giản năm 2026', 'Thắt lưng da, túi đeo chéo và đồng hồ dây vải tinh tế.', 63, DATE_SUB(NOW(), INTERVAL 6 HOUR)
    UNION ALL SELECT 'Bí quyết chọn chất liệu cotton thoáng mát mùa hè', 'So sánh cotton 100%, cotton compact và vải linen tự nhiên.', 112, DATE_SUB(NOW(), INTERVAL 5 HOUR)
    UNION ALL SELECT '3 kiểu phối áo polo trẻ trung cuối tuần', 'Kết hợp áo polo với quần chinos, short hoặc denim.', 88, DATE_SUB(NOW(), INTERVAL 4 HOUR)
    UNION ALL SELECT 'Hướng dẫn vệ sinh giày trắng tại nhà', 'Cách làm sạch đế và thân giày nhanh chóng không làm ố vàng.', 245, DATE_SUB(NOW(), INTERVAL 3 HOUR)
    UNION ALL SELECT 'Lookbook dạo phố phong cách Streetwear', 'Gợi ý outfit năng động với hoodie oversize và quần cargo.', 190, DATE_SUB(NOW(), INTERVAL 2 HOUR)
    UNION ALL SELECT 'Tổng hợp ưu đãi thành viên mới ShopMVC', 'Hướng dẫn đăng ký, kích hoạt OTP và theo dõi danh mục mới.', 320, DATE_SUB(NOW(), INTERVAL 1 HOUR)
) v
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM (SELECT video_id FROM Videos LIMIT 1) AS existing_videos);
