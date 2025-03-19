package vn.com.iuh.fit.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryValidationItem {
    private Long productId;
    private Integer quantity;
}
