package vn.com.iuh.fit.cart_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import vn.com.iuh.fit.cart_service.dto.CartItemDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutEvent {
    private String eventId;
    private String userId;
    private List<CartItemDTO> items;
    private double totalPrice;
    private String status;
    private long timestamp;
    private String correlationId;
}
