package vn.com.iuh.fit.cart_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.com.iuh.fit.cart_service.dto.ProductDTO;

@FeignClient(name = "product-service", url = "http://api-gateway:9000")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") String productId);
}
