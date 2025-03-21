package vn.com.iuh.fit.product_service.entity;

import jakarta.persistence.Column;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Document(indexName = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDocument {
    @Id
    private Long id;
    private String name;

    @Column(columnDefinition = "LONGTEXT")
    private String description;
    private double price;
    private Long categoryId;
    private List<String> imageUrls;
}
