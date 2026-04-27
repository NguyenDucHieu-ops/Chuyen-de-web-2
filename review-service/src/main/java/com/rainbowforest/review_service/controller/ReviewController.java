package com.rainbowforest.review_service.controller;

import com.rainbowforest.review_service.entity.Review;
import com.rainbowforest.review_service.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@CrossOrigin("*") // Mở toang cửa cho Frontend
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewRepository.findAll());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    // 🔥 CHIÊU CUỐI: Hút ID thẳng từ đường dẫn URL (?productId=...)
    @PostMapping
    public ResponseEntity<?> addReview(
            @RequestParam(value = "productId", required = false) Long paramProductId,
            @RequestBody Review review,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            // Ép cứng ID vào sản phẩm (Đố thằng nào làm NULL được nữa)
            if (paramProductId != null) {
                review.setProductId(paramProductId);
            }

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