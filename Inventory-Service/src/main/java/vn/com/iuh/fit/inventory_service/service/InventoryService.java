package vn.com.iuh.fit.inventory_service.service;

import vn.com.iuh.fit.inventory_service.dto.InventoryValidationItem;
import vn.com.iuh.fit.inventory_service.event.InventoryDeductionRequestEvent;

import java.util.List;

public interface InventoryService {
    int getStock(Long productId);
    void updateStock(Long productId, int quantity);
    void validateInventory(Long orderId, List<InventoryValidationItem> items);
    void deductStock(Long orderId, List<InventoryDeductionRequestEvent.ProductQuantity> products);
    void reserveStock(Long orderId, List<InventoryValidationItem> items);
    void releaseStock(Long orderId, List<InventoryValidationItem> items);
}
