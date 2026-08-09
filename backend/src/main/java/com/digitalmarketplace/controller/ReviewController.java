package com.digitalmarketplace.controller;

import com.digitalmarketplace.dto.CreateReviewRequest;
import com.digitalmarketplace.dto.ReviewResponse;
import com.digitalmarketplace.dto.UpdateReviewRequest;
import com.digitalmarketplace.entity.Review;
import com.digitalmarketplace.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Reviews", description = "Product reviews from verified buyers")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/products/{productId}/reviews")
    public List<ReviewResponse> listReviews(
            @PathVariable @Positive(message = "Product id must be positive") Long productId) {
        return reviewService.listByProduct(productId).stream().map(ReviewResponse::from).toList();
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId,
            @PathVariable @Positive(message = "Product id must be positive") Long productId,
            @Valid @RequestBody CreateReviewRequest request) {
        Review review = reviewService.createReview(userId, productId, request.rating(), request.comment());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewResponse.from(review));
    }

    @PutMapping("/reviews/{reviewId}")
    public ReviewResponse updateReview(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId,
            @PathVariable @Positive(message = "Review id must be positive") Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        Review review = reviewService.updateReview(userId, reviewId, request.rating(), request.comment());
        return ReviewResponse.from(review);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId,
            @PathVariable @Positive(message = "Review id must be positive") Long reviewId) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.noContent().build();
    }
}
