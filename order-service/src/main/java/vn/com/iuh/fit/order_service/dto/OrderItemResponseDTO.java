package vn.com.iuh.fit.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import vn.com.iuh.fit.order_service.enums.OrderItemStatus;

@Data
@AllArgsConstructor
public class OrderItemResponseDTO {
    private String productId;
    private String name;
    private Double price;
    private Integer quantity;
    private String imageUrl;
    private OrderItemStatus status;
}

