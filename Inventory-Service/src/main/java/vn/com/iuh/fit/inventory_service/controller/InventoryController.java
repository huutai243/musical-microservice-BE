package vn.com.iuh.fit.inventory_service.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.iuh.fit.inventory_service.dto.InventoryValidationItem;
import vn.com.iuh.fit.inventory_service.service.InventoryService;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public int checkStock(@PathVariable Long productId) {
        return inventoryService.getStock(productId);
    }

    @PostMapping("/{productId}/update-quantity")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateStock(@PathVariable Long productId, @RequestParam int quantity) {
        inventoryService.updateStock(productId, quantity);
    }

    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public void validateInventory(@RequestBody List<InventoryValidationItem> items) {
        inventoryService.validateInventory(null, items);
    }
}
