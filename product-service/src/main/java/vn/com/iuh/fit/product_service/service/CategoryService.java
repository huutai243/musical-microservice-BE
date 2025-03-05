package vn.com.iuh.fit.product_service.service;

import vn.com.iuh.fit.product_service.dto.CategoryDTO;
import vn.com.iuh.fit.product_service.entity.Category;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAllCategories();
    CategoryDTO getCategoryById(Long id);
    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);
}
