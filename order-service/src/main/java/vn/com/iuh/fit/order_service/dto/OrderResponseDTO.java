package vn.com.iuh.fit.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponseDTO {
    private Long orderId;
    private String userId;
    private Double totalPrice;
    private String status;
    private List<OrderItemDTO> items;
}
