package vn.com.iuh.fit.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.product_service.entity.ProductImage;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);
}

