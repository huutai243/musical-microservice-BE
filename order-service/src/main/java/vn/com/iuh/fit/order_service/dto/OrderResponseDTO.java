package vn.com.iuh.fit.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderResponseDTO {
    private Long orderId;
    private String userId;
    private String productId;
    private Integer quantity;
    private Double price;
    private String status;
}
