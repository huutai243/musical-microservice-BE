package vn.com.iuh.fit.review_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    public Optional<Review> getReviewById(ObjectId id) {
        return reviewRepository.findById(id);
    }

    public Page<Review> getReviewsByProductId(String productId, int page, int size) {
        return reviewRepository.findByProductId(productId, PageRequest.of(page, size));
    }

    public Page<Review> getAllReviews(int page, int size) {
        return reviewRepository.findAll(PageRequest.of(page, size));
    }

    public Page<Review> getReviewsByUserId(String userId, int page, int size) {
        return reviewRepository.findByUserId(userId, PageRequest.of(page, size));
    }

    public Review updateReview(Review review) {
        return reviewRepository.save(review);
    }

    public void deleteReview(ObjectId id) {
        reviewRepository.deleteById(id);
    }
}
