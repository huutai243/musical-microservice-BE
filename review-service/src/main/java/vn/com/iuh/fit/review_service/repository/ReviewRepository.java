package vn.com.iuh.fit.review_service.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import vn.com.iuh.fit.review_service.entity.Review;

public interface ReviewRepository extends MongoRepository<Review, ObjectId> {
    Page<Review> findByProductId(String productId, Pageable pageable);
    Page<Review> findByUserId(String userId, Pageable pageable);
}
