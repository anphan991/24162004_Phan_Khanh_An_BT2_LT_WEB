package lt_web.services.impl;

import java.io.IOException;
import java.util.List;
import lt_web.entity.Category;
import lt_web.repository.ICategoryRepository;
import lt_web.repository.impl.CategoryRepository;
import lt_web.services.CategoryService;
import lt_web.utils.FileStorage;

public class CategoryServiceImpl implements CategoryService {
    private final ICategoryRepository categoryRepo;

    public CategoryServiceImpl() {
        this(new CategoryRepository());
    }

    public CategoryServiceImpl(ICategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @Override
    public void insert(Category category) {
        categoryRepo.insert(category);
    }

    @Override
    public void edit(Category newCategory) {
        Category oldCategory = categoryRepo.get(newCategory.getId());
        if (oldCategory == null) {
            return;
        }
        String previousIcon = oldCategory.getIcon();
        oldCategory.setName(newCategory.getName());
        boolean replacingIcon = newCategory.getIcon() != null && !newCategory.getIcon().isBlank();
        if (replacingIcon) {
            oldCategory.setIcon(newCategory.getIcon());
        }
        categoryRepo.edit(oldCategory);

        if (replacingIcon && isCustomIcon(previousIcon) && !previousIcon.equals(newCategory.getIcon())) {
            try {
                FileStorage.delete(previousIcon);
            } catch (IOException e) {
                throw new IllegalStateException("Không thể xóa ảnh danh mục cũ.", e);
            }
        }
    }

    @Override
    public void delete(int id) {
        Category existing = categoryRepo.get(id);
        categoryRepo.delete(id);
        if (existing != null) {
            FileStorage.deleteQuietly(existing.getIcon());
        }
    }

    @Override
    public Category get(int id) {
        return categoryRepo.get(id);
    }

    @Override
    public Category get(String name) {
        return categoryRepo.get(name);
    }

    @Override
    public List<Category> getAll() {
        return categoryRepo.findAll();
    }

    @Override
    public List<Category> search(String catename) {
        return categoryRepo.search(catename);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepo.findAll();
    }

    @Override
    public Category findById(int id) {
        return categoryRepo.findById(id);
    }

    @Override
    public void create(Category category) {
        categoryRepo.create(category);
    }

    @Override
    public void update(Category category) {
        edit(category);
    }

    @Override
    public List<Category> findByCategoryname(String catname) {
        return categoryRepo.findByCategoryname(catname);
    }

    @Override
    public List<Category> findByCategoryname(String catname, int page, int pagesize) {
        return categoryRepo.findByCategoryname(catname, page, pagesize);
    }

    @Override
    public List<Category> findAll(int page, int pagesize) {
        return categoryRepo.findAll(page, pagesize);
    }

    @Override
    public int count() {
        return categoryRepo.count();
    }

    @Override
    public int countByCategoryname(String catname) {
        return categoryRepo.countByCategoryname(catname);
    }

    private boolean isCustomIcon(String fileName) {
        return fileName != null && !fileName.isBlank() && !fileName.startsWith("default-");
    }
}
