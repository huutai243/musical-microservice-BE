package vn.com.iuh.fit.review_service.controller;

import vn.com.iuh.fit.review_service.entity.Review;
import vn.com.iuh.fit.review_service.service.ReviewService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }
    // ✅ API kiểm tra service
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Review Service is running...");
    }

    // ✅ Lấy tất cả đánh giá
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    // ✅ Lấy danh sách đánh giá theo sản phẩm
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProductId(@PathVariable String productId) {
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    // ✅ Lấy đánh giá theo ID (chuyển đổi String -> ObjectId)
    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable String id) {
        if (!ObjectId.isValid(id)) {
            return ResponseEntity.badRequest().body("Invalid review ID format");
        }

        Optional<Review> review = reviewService.getReviewById(new ObjectId(id));
        return review.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Thêm mới đánh giá
    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.addReview(review));
    }

    // ✅ Cập nhật đánh giá (chuyển đổi String -> ObjectId)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable String id, @RequestBody Review updatedReview) {
        if (!ObjectId.isValid(id)) {
            return ResponseEntity.badRequest().body("Invalid review ID format");
        }

        return ResponseEntity.ok(reviewService.updateReview(new ObjectId(id), updatedReview));
    }

    // ✅ Xóa đánh giá theo ID (chuyển đổi String -> ObjectId)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable String id) {
        if (!ObjectId.isValid(id)) {
            return ResponseEntity.badRequest().body("Invalid review ID format");
        }

        reviewService.deleteReview(new ObjectId(id));
        return ResponseEntity.noContent().build();
    }
}
