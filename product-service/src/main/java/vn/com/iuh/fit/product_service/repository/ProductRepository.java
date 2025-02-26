package vn.com.iuh.fit.product_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.iuh.fit.product_service.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}

