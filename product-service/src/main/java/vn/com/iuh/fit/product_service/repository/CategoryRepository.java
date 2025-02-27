package vn.com.iuh.fit.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.product_service.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
