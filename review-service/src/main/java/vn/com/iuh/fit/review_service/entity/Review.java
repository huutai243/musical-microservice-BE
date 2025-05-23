package vn.com.iuh.fit.review_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonIgnore // Ẩn ObjectId gốc trong JSON
    private ObjectId id;

    @Indexed
    private String userId;

    @Indexed
    private String productId;

    private String comment;
    private int rating;

    @JsonProperty("id") // Dùng tên "id" khi serialize thành JSON
    public String getIdAsString() {
        return id != null ? id.toHexString() : null;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

}
