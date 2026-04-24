package com.rainbowforest.review_service.controller;

import com.rainbowforest.review_service.entity.Review;
import com.rainbowforest.review_service.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    // ✅ THÊM HÀM NÀY ĐỂ HIỆN ĐÁNH GIÁ RA TRANG CHỦ (HẾT LỖI 405)
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewRepository.findAll());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Review review,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            if (userName != null && !userName.isEmpty()) {
                review.setUserName(userName);
            } else if (review.getUserName() == null) {
                review.setUserName("Khách ẩn danh");
            }

            if (review.getRating() < 1)
                review.setRating(1);
            if (review.getRating() > 5)
                review.setRating(5);

            return ResponseEntity.ok(reviewRepository.save(review));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}