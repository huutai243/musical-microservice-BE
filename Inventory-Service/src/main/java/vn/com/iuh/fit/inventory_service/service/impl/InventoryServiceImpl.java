package vn.com.iuh.fit.inventory_service.service.impl;

import vn.com.iuh.fit.inventory_service.repository.InventoryRepository;
import vn.com.iuh.fit.inventory_service.service.InventoryService;
import vn.com.iuh.fit.inventory_service.entity.Inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public int getStock(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .map(Inventory::getQuantity)
                .orElse(0); // Trả về 0 nếu không tìm thấy sản phẩm
    }

    @Transactional
    @Override
    public void updateStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(new Inventory(null, productId, 0));

        inventory.setQuantity(quantity);
        inventoryRepository.save(inventory);
    }
}
