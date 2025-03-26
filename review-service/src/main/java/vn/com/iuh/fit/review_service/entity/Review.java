package vn.com.iuh.fit.review_service.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "reviews") // Chỉ định collection trong MongoDB
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    private ObjectId id;  // Dùng ObjectId thay vì String cho MongoDB

    @Indexed
    private String userId;

    @Indexed
    private String productId;

    private String comment;
    private int rating;
}
