package vn.com.iuh.fit.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;             // ID sản phẩm
    private String name;         // Tên sản phẩm
    private String description;  // Mô tả sản phẩm
    private double price;        // Giá sản phẩm
    private int stockQuantity;   // Số lượng tồn kho
    private String imageUrl;     // URL ảnh sản phẩm (trả về để hiển thị)
    private String categoryName; // Tên danh mục sản phẩm (hiển thị trên UI)
}
