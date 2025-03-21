package vn.com.iuh.fit.product_service.repository;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import vn.com.iuh.fit.product_service.entity.ProductDocument;

import java.util.List;

public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    List<ProductDocument> findByNameContainingIgnoreCase(String keyword);
    // Tìm theo ID danh mục
    @Query("{\"match\": {\"categoryId\": \"?0\"}}")
    List<ProductDocument> findByCategoryId(Long categoryId);

    // Tìm sản phẩm theo khoảng giá
    List<ProductDocument> findByPriceBetween(double minPrice, double maxPrice);
}
