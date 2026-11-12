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
        oldCategory.setName(newCategory.getName());
        if (newCategory.getIcon() != null && !newCategory.getIcon().isBlank()) {
            String fileName = oldCategory.getIcon();
            if (fileName != null && !fileName.equals(newCategory.getIcon())
                    && !fileName.startsWith("default-")) {
                try {
                    FileStorage.delete(fileName);
                } catch (IOException e) {
                    throw new IllegalStateException("Không thể xóa ảnh danh mục cũ.", e);
                }
            }
            oldCategory.setIcon(newCategory.getIcon());
        }
        categoryRepo.edit(oldCategory);
    }

    @Override
    public void delete(int id) {
        categoryRepo.delete(id);
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
    public List<Category> findAll(int page, int pagesize) {
        return categoryRepo.findAll(page, pagesize);
    }

    @Override
    public int count() {
        return categoryRepo.count();
    }
}
