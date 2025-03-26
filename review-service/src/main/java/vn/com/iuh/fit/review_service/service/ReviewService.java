package vn.com.iuh.fit.review_service.service;

import vn.com.iuh.fit.review_service.entity.Review;
import vn.com.iuh.fit.review_service.repository.ReviewRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // ✅ Lấy tất cả đánh giá
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // ✅ Lấy danh sách đánh giá theo sản phẩm
    public List<Review> getReviewsByProductId(String productId) {
        return reviewRepository.findByProductId(productId);
    }

    // ✅ Lấy đánh giá theo ID (dùng ObjectId thay vì String)
    public Optional<Review> getReviewById(ObjectId id) {
        return reviewRepository.findById(id);
    }

    // ✅ Thêm mới đánh giá
    public Review addReview(Review review) {
        return reviewRepository.save(review);
    }

    // ✅ Cập nhật đánh giá (chuyển đổi String -> ObjectId)
    public Review updateReview(ObjectId id, Review updatedReview) {
        if (reviewRepository.existsById(id)) {
            updatedReview.setId(id);
            return reviewRepository.save(updatedReview);
        }
        return null; // Trả về null nếu không tìm thấy
    }

    // ✅ Xóa đánh giá theo ID (dùng ObjectId)
    public void deleteReview(ObjectId id) {
        reviewRepository.deleteById(id);
    }
}
