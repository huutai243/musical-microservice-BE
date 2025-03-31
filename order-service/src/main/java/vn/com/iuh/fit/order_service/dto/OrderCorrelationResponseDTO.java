package vn.com.iuh.fit.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import vn.com.iuh.fit.order_service.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderCorrelationResponseDTO {
    private Long orderId;
    private String userId;
    private Double totalPrice;
    private OrderStatus status;
    private String correlationId;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDTO> items;
}

