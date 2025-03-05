package vn.com.iuh.fit.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private List<String> productNames;
}


