package vn.com.iuh.fit.product_service.dto;

import lombok.Data;

@Data
public class ProductRequest {
    private String name;         // Tên sản phẩm
    private String description;  // Mô tả sản phẩm
    private double price;        // Giá sản phẩm
    private Long categoryId;     // ID danh mục sản phẩm (bắt buộc khi thêm/sửa)
}
