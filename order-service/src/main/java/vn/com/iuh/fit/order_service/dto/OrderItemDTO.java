package vn.com.iuh.fit.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private String productId;
    private String name;
    private Double price;
    private Integer quantity;
    private String imageUrl;
}
