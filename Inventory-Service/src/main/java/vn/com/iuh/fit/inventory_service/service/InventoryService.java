package vn.com.iuh.fit.inventory_service.service;

public interface InventoryService {
    int getStock(Long productId);
    void updateStock(Long productId, int quantity);
}
