package vn.com.iuh.fit.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.product_service.entity.Product;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Product> findByPriceBetween(double minPrice, double maxPrice, Pageable pageable);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    List<Product> findTopByOrderByIdDesc(); // Lấy sản phẩm mới nhất
//    List<Product> findTopByOrderByStockQuantityDesc(); // Lấy sản phẩm bán chạy
}
