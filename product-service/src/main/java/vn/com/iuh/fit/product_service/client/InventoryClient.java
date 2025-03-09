package vn.com.iuh.fit.product_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service", url = "http://inventory-service:8083/api/inventory")
public interface InventoryClient {
    @GetMapping("/{productId}")
    int getStock(@PathVariable Long productId);
}
