package vn.com.iuh.fit.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.iuh.fit.product_service.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
