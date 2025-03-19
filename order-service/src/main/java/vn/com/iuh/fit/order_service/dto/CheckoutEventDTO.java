package vn.com.iuh.fit.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutEventDTO {
    private String eventId;
    private String userId;
    private List<CartItemDTO> items;
    private double totalPrice;
    private String status;
    private long timestamp;
    private String correlationId;
}
