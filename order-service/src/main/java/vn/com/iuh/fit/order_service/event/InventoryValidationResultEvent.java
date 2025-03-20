package vn.com.iuh.fit.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryValidationResultEvent {
    private Long orderId;
    private String status; // VALIDATED, PARTIALLY_VALIDATED, REJECTED
    private String message;
    private List<Item> validatedItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String productId;
        private Integer requestedQuantity;
        private Integer availableQuantity;
        private String status; // CONFIRMED, OUT_OF_STOCK
    }
}

