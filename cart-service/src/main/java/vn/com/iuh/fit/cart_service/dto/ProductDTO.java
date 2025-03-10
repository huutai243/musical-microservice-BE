package vn.com.iuh.fit.cart_service.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private double price;
    private String imageUrl;
}
