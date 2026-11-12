package lt_web.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lt_web.entity.Category;
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
