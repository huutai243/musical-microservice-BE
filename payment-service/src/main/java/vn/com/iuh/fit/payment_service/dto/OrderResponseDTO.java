package vn.com.iuh.fit.payment_service.dto;

import lombok.Data;
import vn.com.iuh.fit.payment_service.enums.OrderStatus;

@Data
public class OrderResponseDTO {
    private Long orderId;
    private Double totalPrice;
    private String userId;
    private OrderStatus status;
}
