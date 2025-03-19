package vn.com.iuh.fit.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private String productId;
    private String name;
    private double price;
    private int requestedQuantity;
    private String imageUrl;
}
