package lt_web.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.List;
import lt_web.entity.RememberToken;
import lt_web.entity.User;
import lt_web.exceptions.DataAccessException;
import lt_web.repository.IRememberTokenRepository;
import lt_web.repository.JpaConfig;

public class RememberTokenRepository implements IRememberTokenRepository {

    @Override
    public void save(String tokenHash, int userId, Timestamp expiresAt) {
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            RememberToken token = new RememberToken(
                    tokenHash,
                    userId,
                    expiresAt,
                    new Timestamp(System.currentTimeMillis()));
            enma.merge(token);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể lưu phiên đăng nhập ghi nhớ.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public User findValidUser(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            return null;
        }
        EntityManager enma = JpaConfig.getEntityManager();
        try {
            String jpql = "SELECT u FROM RememberToken t JOIN User u ON u.id = t.userId "
                    + "WHERE t.tokenHash = :tokenHash AND t.expiresAt > :now";
            TypedQuery<User> query = enma.createQuery(jpql, User.class);
            query.setParameter("tokenHash", tokenHash);
            query.setParameter("now", new Timestamp(System.currentTimeMillis()));
            List<User> results = query.setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            throw new DataAccessException("Không thể xác thực Cookie ghi nhớ.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public void delete(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            return;
        }
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            RememberToken token = enma.find(RememberToken.class, tokenHash);
            if (token != null) {
                enma.remove(token);
            }
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể thu hồi Cookie ghi nhớ.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public void deleteExpired() {
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            enma.createQuery("DELETE FROM RememberToken t WHERE t.expiresAt <= :now")
                    .setParameter("now", new Timestamp(System.currentTimeMillis()))
                    .executeUpdate();
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể dọn Cookie ghi nhớ hết hạn.", e);
        } finally {
            enma.close();
        }
    }
}
