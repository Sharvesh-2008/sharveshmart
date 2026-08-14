package com.sharveshmart.controller;

import com.sharveshmart.dto.CreateReviewRequest;
import com.sharveshmart.dto.ReviewResponse;
import com.sharveshmart.dto.UpdateReviewRequest;
import com.sharveshmart.entity.Review;
import com.sharveshmart.security.UserPrincipal;
import com.sharveshmart.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "Product id must be positive") Long productId,
            @Valid @RequestBody CreateReviewRequest request) {
        Review review = reviewService.createReview(principal.getId(), productId, request.rating(), request.comment());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewResponse.from(review));
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ReviewResponse updateReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "Review id must be positive") Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        Review review = reviewService.updateReview(principal.getId(), reviewId, request.rating(), request.comment());
        return ReviewResponse.from(review);
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "Review id must be positive") Long reviewId) {
        reviewService.deleteReview(principal.getId(), reviewId);
        return ResponseEntity.noContent().build();
    }
}
