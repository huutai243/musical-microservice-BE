package vn.com.iuh.fit.cart_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.iuh.fit.cart_service.dto.CartItemDTO;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutEvent {
    private UUID eventId;
    private String userId;
    private List<CartItemDTO> items;
    private double totalPrice;
    private String status;
    private long timestamp;
    private String correlationId;

    public CheckoutEvent(String userId, List<CartItemDTO> items, double totalPrice) {
        this.eventId = UUID.randomUUID();
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = "PENDING";
        this.timestamp = System.currentTimeMillis();
        this.correlationId = UUID.randomUUID().toString();
    }
}
