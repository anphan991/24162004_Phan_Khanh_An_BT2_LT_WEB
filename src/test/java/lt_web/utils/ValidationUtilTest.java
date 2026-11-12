package lt_web.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import lt_web.entity.Category;

class ValidationUtilTest {

    @Test
    void rejectsBlankCategoryName() {
        Category category = new Category();
        category.setCatename(" ");

        assertFalse(ValidationUtil.validate(category).isEmpty());
    }

    @Test
    void acceptsValidCategory() {
        Category category = new Category();
        category.setCatename("Quần áo");
        category.setIcon("ao.jpg");

        assertTrue(ValidationUtil.validate(category).isEmpty());
    }
}
