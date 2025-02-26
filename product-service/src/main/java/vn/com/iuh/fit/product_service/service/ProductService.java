package vn.com.iuh.fit.product_service.service;

import org.springframework.web.multipart.MultipartFile;
import vn.com.iuh.fit.product_service.dto.ProductRequest;
import vn.com.iuh.fit.product_service.dto.ProductResponse;
import java.util.List;


public interface ProductService {
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Long id);
    ProductResponse addProduct(ProductRequest productRequest);
    void deleteProduct(Long id);
    String uploadFile(MultipartFile file) throws Exception;
}



