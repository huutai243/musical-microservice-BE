package vn.com.iuh.fit.inventory_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryValidationResultEvent {
    private Long orderId;
    private String status; // "VALIDATED" hoặc "REJECTED"
    private String message;
}
