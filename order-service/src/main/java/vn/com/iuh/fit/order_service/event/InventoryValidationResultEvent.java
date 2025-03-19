package vn.com.iuh.fit.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryValidationResultEvent {
    private Long orderId;
    private String status;
    private String message;
}

