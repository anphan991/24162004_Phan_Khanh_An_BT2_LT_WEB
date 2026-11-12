package lt_web.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.List;
import lt_web.entity.User;
import lt_web.exceptions.DataAccessException;
import lt_web.repository.IUserRepository;
import lt_web.repository.JpaConfig;
import lt_web.utils.PasswordHasher;

public class UserRepository implements IUserRepository {

    @Override
    public List<User> findAll() {
        EntityManager enma = JpaConfig.getEntityManager();
        try {
            TypedQuery<User> query = enma.createNamedQuery("User.findAll", User.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh sách tài khoản.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public User findById(int id) {
        EntityManager enma = JpaConfig.getEntityManager();
        try {
            return enma.find(User.class, id);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải tài khoản theo ID.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public User findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        EntityManager enma = JpaConfig.getEntityManager();
        try {
            TypedQuery<User> query = enma.createQuery(
                    "SELECT u FROM User u WHERE u.userName = :username", User.class);
            query.setParameter("username", username.trim());
            List<User> results = query.setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải tài khoản theo tên đăng nhập.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public User get(String username) {
        return findByUsername(username);
    }

    @Override
    public User getById(int id) {
        return findById(id);
    }

    @Override
    public void insert(User user) {
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            if (user.getRoleid() <= 0) {
                user.setRoleid(5);
            }
            enma.persist(user);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể thêm tài khoản.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public void update(User user) {
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            enma.merge(user);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể cập nhật tài khoản.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public void updatePassword(String username, String passwordHash) {
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            enma.createQuery("UPDATE User u SET u.passWord = :password WHERE u.userName = :username")
                    .setParameter("password", passwordHash)
                    .setParameter("username", username)
                    .executeUpdate();
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể nâng cấp mật khẩu tài khoản.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public int migratePlaintextPasswords() {
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            List<User> users = enma.createQuery(
                    "SELECT u FROM User u WHERE u.passWord IS NOT NULL AND u.passWord <> ''", User.class)
                    .getResultList();
            int migrated = 0;
            for (User user : users) {
                String stored = user.getPassWord();
                if (!PasswordHasher.isHash(stored)) {
                    enma.createQuery("UPDATE User u SET u.passWord = :password WHERE u.id = :id")
                            .setParameter("password", PasswordHasher.hash(stored))
                            .setParameter("id", user.getId())
                            .executeUpdate();
                    migrated++;
                }
            }
            trans.commit();
            return migrated;
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể mã hóa các mật khẩu cũ.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public void delete(int id) {
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            User user = enma.find(User.class, id);
            if (user != null) {
                enma.remove(user);
            }
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể xóa tài khoản.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public boolean checkExistEmail(String email) {
        return checkExistEmail(email, 0);
    }

    @Override
    public boolean checkExistEmail(String email, int excludedId) {
        return exists("email", email, excludedId);
    }

    @Override
    public boolean checkExistUsername(String username) {
        return checkExistUsername(username, 0);
    }

    @Override
    public boolean checkExistUsername(String username, int excludedId) {
        return exists("userName", username, excludedId);
    }

    @Override
    public boolean checkExistPhone(String phone) {
        return checkExistPhone(phone, 0);
    }

    @Override
    public boolean checkExistPhone(String phone, int excludedId) {
        return exists("phone", phone, excludedId);
    }

    private boolean exists(String fieldName, String value, int excludedId) {
        if (value == null || value.isBlank()) {
            return false;
        }
        EntityManager enma = JpaConfig.getEntityManager();
        try {
            String jpql = "SELECT COUNT(u) FROM User u WHERE u." + fieldName + " = :value";
            if (excludedId > 0) {
                jpql += " AND u.id <> :excludedId";
            }
            TypedQuery<Long> query = enma.createQuery(jpql, Long.class);
            query.setParameter("value", value.trim());
            if (excludedId > 0) {
                query.setParameter("excludedId", excludedId);
            }
            Long count = query.getSingleResult();
            return count != null && count > 0;
        } catch (Exception e) {
            throw new DataAccessException("Không thể kiểm tra dữ liệu tài khoản.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public List<User> getAll() {
        return findAll();
    }
}
