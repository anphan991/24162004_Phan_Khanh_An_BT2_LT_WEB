package lt_web.repository.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import lt_web.entity.OtpToken;
import lt_web.exceptions.DataAccessException;
import lt_web.repository.IOtpRepository;
import lt_web.repository.JpaConfig;

public class OtpRepository implements IOtpRepository {

    @Override
    public void create(OtpToken token) {
        if (token == null) {
            return;
        }
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            enma.persist(token);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể lưu mã OTP.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public OtpToken findLatestValid(String email, String otpType, LocalDateTime now) {
        if (email == null || email.isBlank() || otpType == null) {
            return null;
        }
        EntityManager enma = JpaConfig.getEntityManager();
        try {
            TypedQuery<OtpToken> query = enma.createNamedQuery("OtpToken.findLatestValid", OtpToken.class);
            query.setParameter("email", email.trim().toLowerCase(Locale.ROOT));
            query.setParameter("otpType", otpType);
            query.setParameter("now", now != null ? now : LocalDateTime.now());
            List<OtpToken> results = query.setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tìm mã OTP hợp lệ.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public void markUsed(int id) {
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            OtpToken token = enma.find(OtpToken.class, id);
            if (token != null) {
                token.setUsed(true);
            }
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể đánh dấu mã OTP đã sử dụng.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public void invalidatePrevious(String email, String otpType) {
        if (email == null || email.isBlank() || otpType == null) {
            return;
        }
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            enma.createQuery("UPDATE OtpToken o SET o.used = true WHERE LOWER(TRIM(o.email)) = :email AND o.otpType = :otpType AND o.used = false")
                    .setParameter("email", email.trim().toLowerCase(Locale.ROOT))
                    .setParameter("otpType", otpType)
                    .executeUpdate();
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể vô hiệu hóa mã OTP cũ.", e);
        } finally {
            enma.close();
        }
    }
}
