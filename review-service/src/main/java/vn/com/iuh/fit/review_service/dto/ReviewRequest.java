package vn.com.iuh.fit.review_service.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long productId;
    private String content;
    private int rating;
}
