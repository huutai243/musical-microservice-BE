package vn.com.iuh.fit.review_service.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import vn.com.iuh.fit.review_service.entity.Review;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, ObjectId> {
    List<Review> findByProductId(String productId);
}
