package lt_web.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lt_web.entity.Category;
import lt_web.entity.User;
import lt_web.entity.Video;
import lt_web.exceptions.DataAccessException;

public final class JpaConfig {
    private static final String PERSISTENCE_UNIT = "jpa-hibernate-mysql";
    private static final String DEFAULT_URL =
            "jdbc:mysql://127.0.0.1:3306/bt_lt_web"
            + "?createDatabaseIfNotExist=true"
            + "&useSSL=false"
            + "&serverTimezone=Asia/Ho_Chi_Minh"
            + "&allowPublicKeyRetrieval=true"
            + "&characterEncoding=UTF-8"
            + "&connectTimeout=3000"
            + "&socketTimeout=5000";
    private static final Properties CONFIG = loadConfig();
    private static final String URL = setting("db.url", "bt.lt.db.url", "BT_LT_DB_URL", DEFAULT_URL);
    private static final String USER = setting("db.user", "bt.lt.db.user", "BT_LT_DB_USER", "root");
    private static final String PASSWORD = setting("db.password", "bt.lt.db.password", "BT_LT_DB_PASSWORD", null);

    private static volatile EntityManagerFactory entityManagerFactory;

    private JpaConfig() {
    }

    public static EntityManager getEntityManager() {
        try {
            return getEntityManagerFactory().createEntityManager();
        } catch (RuntimeException e) {
            throw new DataAccessException("Không thể khởi tạo kết nối JPA/Hibernate tới MySQL.", e);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        EntityManagerFactory current = entityManagerFactory;
        if (current == null || !current.isOpen()) {
            synchronized (JpaConfig.class) {
                current = entityManagerFactory;
                if (current == null || !current.isOpen()) {
                    current = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, runtimeProperties());
                    entityManagerFactory = current;
                    seedInitialData(current);
                }
            }
        }
        return current;
    }

    public static void close() {
        synchronized (JpaConfig.class) {
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
            entityManagerFactory = null;
        }
    }

    private static void seedInitialData(EntityManagerFactory factory) {
        try (EntityManager enma = factory.createEntityManager()) {
            EntityTransaction trans = enma.getTransaction();
            try {
                trans.begin();
                enma.createQuery("UPDATE User u SET u.fullName = 'Phan Khánh An', u.roleid = 1 WHERE u.userName = 'admin'")
                        .executeUpdate();
                enma.createQuery("UPDATE User u SET u.roleid = 5 WHERE u.userName <> 'admin' AND (u.roleid IS NULL OR u.roleid = 1)")
                        .executeUpdate();
                enma.createQuery("UPDATE User u SET u.fullName = u.userName WHERE u.fullName IS NULL OR u.fullName = ''")
                        .executeUpdate();

                Long count = enma.createQuery("SELECT COUNT(c) FROM Category c", Long.class).getSingleResult();
                if (count == null || count == 0L) {
                    Category men = new Category();
                    men.setCatename("Quần Áo Nam");
                    men.setIcon("default-men.svg");
                    enma.persist(men);

                    Category women = new Category();
                    women.setCatename("Quần Áo Nữ");
                    women.setIcon("default-women.svg");
                    enma.persist(women);
                }

                seedDefaultYoutubeVideos(enma);
                trans.commit();
            } catch (Exception e) {
                if (trans.isActive()) {
                    trans.rollback();
                }
            }
        } catch (Exception ignored) {
            // Bỏ qua lỗi khởi tạo dữ liệu mẫu để không chặn quá trình tạo EntityManagerFactory.
        }
    }

    public static void seedDefaultYoutubeVideos(EntityManager enma) {
        enma.createQuery("DELETE FROM Video v WHERE v.videoPath = 'default-video.mp4' AND v.poster = 'default-video-poster.svg'")
                .executeUpdate();

        Long youtubeCount = enma.createQuery(
                "SELECT COUNT(v) FROM Video v WHERE v.videoPath LIKE 'https://www.youtube.com/watch?v=%'",
                Long.class).getSingleResult();
        if (youtubeCount != null && youtubeCount > 0L) {
            return;
        }

        List<User> adminOrFirst = enma.createQuery(
                "SELECT u FROM User u ORDER BY CASE WHEN u.userName = 'admin' THEN 0 ELSE 1 END, u.id ASC",
                User.class).setMaxResults(1).getResultList();
        if (adminOrFirst.isEmpty()) {
            return;
        }

        User owner = adminOrFirst.get(0);
        String[][] samples = {
                {"Java in 100 Seconds", "https://www.youtube.com/watch?v=l9AzO1FMgM8", "Tổng quan nhanh về ngôn ngữ lập trình Java, JVM và hệ sinh thái doanh nghiệp.", "340"},
                {"HTML in 100 Seconds", "https://www.youtube.com/watch?v=ok-plXXHlWw", "Cấu trúc tài liệu HTML, các thẻ ngữ nghĩa cơ bản khi xây dựng trang web.", "215"},
                {"CSS in 100 Seconds", "https://www.youtube.com/watch?v=OEV8gMkCHXQ", "Cách định kiểu giao diện, Flexbox, Grid và bố cục responsive cho website.", "189"},
                {"JavaScript in 100 Seconds", "https://www.youtube.com/watch?v=DHjqpvDnNGE", "Giới thiệu nhanh về JavaScript, DOM, xử lý sự kiện và bất đồng bộ.", "412"},
                {"SQL in 100 Seconds", "https://www.youtube.com/watch?v=zsjvFFKOm3c", "Nguyên lý cơ sở dữ liệu quan hệ, khóa chính, khóa ngoại và truy vấn SQL.", "276"},
                {"Git Explained in 100 Seconds", "https://www.youtube.com/watch?v=hwP7WQkmECE", "Cách quản lý phiên bản mã nguồn với commit, branch, merge và push lên GitHub.", "198"},
                {"Docker in 100 Seconds", "https://www.youtube.com/watch?v=Gjnup-PuquQ", "Đóng gói và triển khai ứng dụng nhất quán bằng Container và Dockerfile.", "164"},
                {"REST API Concepts and Examples", "https://www.youtube.com/watch?v=-MTSQjw5DrM", "Tìm hiểu kiến trúc RESTful API, các phương thức GET/POST/PUT/DELETE và JSON.", "230"},
                {"HTTP Crash Course & Exploration", "https://www.youtube.com/watch?v=iYM2zFP3Zn0", "Cơ chế request/response, header, status code và HTTPS trong lập trình Web.", "152"},
                {"MySQL — The Basics in 100 Seconds", "https://www.youtube.com/watch?v=Cz3WcZLRaWc", "Thực hành tạo bảng, liên kết quan hệ 1-N và truy vấn dữ liệu trong MySQL.", "295"}
        };
        LocalDateTime baseTime = LocalDateTime.now().minusDays(2);
        for (int i = 0; i < samples.length; i++) {
            Video sample = new Video(
                    0,
                    samples[i][0],
                    "default-video-poster.svg",
                    samples[i][1],
                    samples[i][2],
                    Integer.parseInt(samples[i][3]),
                    1,
                    baseTime.plusHours(i),
                    owner
            );
            enma.persist(sample);
        }
    }

    private static Map<String, Object> runtimeProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", URL);
        properties.put("jakarta.persistence.jdbc.user", USER);
        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình mật khẩu MySQL cho JPA/Hibernate.");
        }
        properties.put("jakarta.persistence.jdbc.password", PASSWORD);
        return properties;
    }

    private static Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = JpaConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        return properties;
    }

    private static String setting(String fileKey, String systemKey, String envKey, String fallback) {
        String systemValue = System.getProperty(systemKey);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        String fileValue = CONFIG.getProperty(fileKey);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue.trim();
        }
        return fallback;
    }

    public static void main(String[] args) {
        try (EntityManager enma = getEntityManager()) {
            Long count = enma.createQuery("SELECT COUNT(c) FROM Category c", Long.class).getSingleResult();
            System.out.println("Kết nối JPA/Hibernate thành công! Số danh mục hiện có: " + count);
        } finally {
            close();
        }
    }
}
