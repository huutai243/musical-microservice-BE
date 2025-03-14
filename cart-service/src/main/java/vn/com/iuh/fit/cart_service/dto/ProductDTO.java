package vn.com.iuh.fit.cart_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private double price;

    @JsonProperty("imageUrls")
    private List<String> imageUrls;

    public String getFirstImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }
}
