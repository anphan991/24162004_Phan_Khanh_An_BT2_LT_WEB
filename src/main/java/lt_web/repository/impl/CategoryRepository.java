package lt_web.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.List;
import lt_web.entity.Category;
import lt_web.exceptions.DataAccessException;
import lt_web.repository.ICategoryRepository;
import lt_web.repository.JpaConfig;

public class CategoryRepository implements ICategoryRepository {

    @Override
    public List<Category> findAll() {
        EntityManager enma = JpaConfig.getEntityManager();
        try {
            TypedQuery<Category> query = enma.createNamedQuery("Category.findAll", Category.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh mục từ MySQL qua JPA.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public Category findById(int id) {
        EntityManager enma = JpaConfig.getEntityManager();
        try {
            return enma.find(Category.class, id);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh mục theo ID.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public void create(Category category) {
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            enma.persist(category);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể thêm danh mục.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public void update(Category category) {
        EntityManager enma = JpaConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            enma.merge(category);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể cập nhật danh mục.", e);
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
            Category category = enma.find(Category.class, id);
            if (category != null) {
                enma.remove(category);
            }
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw new DataAccessException("Không thể xóa danh mục.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public List<Category> findByCategoryname(String catname) {
        EntityManager enma = JpaConfig.getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.catename LIKE :catname ORDER BY c.cateid ASC";
            TypedQuery<Category> query = enma.createQuery(jpql, Category.class);
            query.setParameter("catname", "%" + (catname == null ? "" : catname.trim()) + "%");
            return query.getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể tìm danh mục từ MySQL qua JPA.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public List<Category> findAll(int page, int pagesize) {
        EntityManager enma = JpaConfig.getEntityManager();
        try {
            TypedQuery<Category> query = enma.createNamedQuery("Category.findAll", Category.class);
            query.setFirstResult(page * pagesize);
            query.setMaxResults(pagesize);
            return query.getResultList();
        } catch (Exception e) {
            throw new DataAccessException("Không thể phân trang danh mục.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public int count() {
        EntityManager enma = JpaConfig.getEntityManager();
        try {
            String jpql = "SELECT count(c) FROM Category c";
            Query query = enma.createQuery(jpql);
            return ((Long) query.getSingleResult()).intValue();
        } catch (Exception e) {
            throw new DataAccessException("Không thể đếm số lượng danh mục.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public void insert(Category category) {
        create(category);
    }

    @Override
    public void edit(Category category) {
        update(category);
    }

    @Override
    public Category get(int id) {
        return findById(id);
    }

    @Override
    public Category get(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        EntityManager enma = JpaConfig.getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.catename = :name";
            TypedQuery<Category> query = enma.createQuery(jpql, Category.class);
            query.setParameter("name", name.trim());
            List<Category> results = query.setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            throw new DataAccessException("Không thể tải danh mục theo tên.", e);
        } finally {
            enma.close();
        }
    }

    @Override
    public List<Category> getAll() {
        return findAll();
    }

    @Override
    public List<Category> search(String keyword) {
        return findByCategoryname(keyword);
    }

    public static void main(String[] args) {
        try {
            ICategoryRepository repo = new CategoryRepository();
            List<Category> list = repo.findAll();
            System.out.println("Danh sách Category qua JPA (" + list.size() + " mục):");
            for (Category c : list) {
                System.out.println(" - ID: " + c.getCateid() + " | Tên: " + c.getCatename() + " | Icon: " + c.getIcon());
            }
        } finally {
            JpaConfig.close();
        }
    }
}
