package vn.com.iuh.fit.cart_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {
    private String productId;
    private String name;
    private double price;
    private int requestedQuantity;
    private String imageUrl;
}
