package vn.com.iuh.fit.review_service.controller;
import org.bson.types.ObjectId;
import vn.com.iuh.fit.review_service.entity.Review;
import vn.com.iuh.fit.review_service.repository.ReviewRepository;
import vn.com.iuh.fit.review_service.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ✅ USER & ADMIN: Thêm review (User chỉ cho chính họ, Admin chỉ cho chính họ)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody Review review, Authentication authentication) {
        String currentUserId = authentication.getName();
        review.setUserId(currentUserId);
        Review savedReview = reviewService.createReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
    }

    // ✅ USER & ADMIN: Lấy tất cả review của một sản phẩm (phân trang)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<Review>> getReviewsByProductId(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviewsByProductId(productId, page, size));
    }

    // ✅ ADMIN: Xem tất cả review (phân trang)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<Review>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getAllReviews(page, size));
    }

    // ✅ ADMIN: Xem review của một user cụ thể (phân trang)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Review>> getReviewsByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviewsByUserId(userId, page, size));
    }

    // ✅ ADMIN: Xem review theo ID
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable ObjectId id) {
        return reviewService.getReviewById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ USER: Cập nhật review của chính họ, ADMIN: Cập nhật bất kỳ review nào
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable ObjectId id, @RequestBody Review review, Authentication authentication) {
        Optional<Review> existingReview = reviewService.getReviewById(id);

        if (existingReview.isPresent()) {
            Review foundReview = existingReview.get();
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
                    foundReview.getUserId().equals(authentication.getName())) {
                review.setId(id);
                return ResponseEntity.ok(reviewService.updateReview(review));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    // ✅ USER: Xóa review của chính họ, ADMIN: Xóa bất kỳ review nào
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable ObjectId id, Authentication authentication) {
        Optional<Review> existingReview = reviewService.getReviewById(id);

        if (existingReview.isPresent()) {
            Review foundReview = existingReview.get();
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
                    foundReview.getUserId().equals(authentication.getName())) {
                reviewService.deleteReview(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.notFound().build();
    }
}

