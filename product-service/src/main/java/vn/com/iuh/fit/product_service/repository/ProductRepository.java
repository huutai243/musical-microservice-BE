package vn.com.iuh.fit.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.product_service.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByNameContainingIgnoreCase(String keyword);
    List<Product> findByPriceBetween(double minPrice, double maxPrice);
    List<Product> findTopByOrderByIdDesc(); // Lấy sản phẩm mới nhất
    List<Product> findTopByOrderByStockQuantityDesc(); // Lấy sản phẩm bán chạy
}
