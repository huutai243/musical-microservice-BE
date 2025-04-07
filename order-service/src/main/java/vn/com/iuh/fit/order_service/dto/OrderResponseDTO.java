package vn.com.iuh.fit.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import vn.com.iuh.fit.order_service.enums.OrderStatus;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponseDTO {
    private Long orderId;
    private Double totalPrice;
    private String userId;
    private OrderStatus status;
    private List<OrderItemResponseDTO> items;
}
