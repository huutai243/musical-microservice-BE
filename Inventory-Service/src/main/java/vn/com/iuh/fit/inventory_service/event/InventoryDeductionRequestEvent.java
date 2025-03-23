package vn.com.iuh.fit.inventory_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDeductionRequestEvent {
    private Long orderId;
    private String userId;
    private List<ProductQuantity> products;
    private String reason;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductQuantity {
        private String productId;
        private Integer quantity;
    }
}

