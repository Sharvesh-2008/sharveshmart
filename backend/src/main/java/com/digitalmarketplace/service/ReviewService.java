package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.PurchaseEntitlement;
import com.digitalmarketplace.entity.Review;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.exception.ResourceNotFoundException;
import com.digitalmarketplace.repository.ProductRepository;
import com.digitalmarketplace.repository.PurchaseEntitlementRepository;
import com.digitalmarketplace.repository.ReviewRepository;
import com.digitalmarketplace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PurchaseEntitlementRepository purchaseEntitlementRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository,
                         PurchaseEntitlementRepository purchaseEntitlementRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.purchaseEntitlementRepository = purchaseEntitlementRepository;
    }

    @Transactional
    public Review createReview(Long userId, Long productId, short rating, String comment) {
        validateRating(rating);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (reviewRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
            throw new BusinessException("A review already exists for this product");
        }
        requirePurchase(userId, productId);

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    @Transactional
    public Review updateReview(Long userId, Long reviewId, short rating, String comment) {
        validateRating(rating);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException("Only the review author can update it");
        }
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException("Only the review author can delete it");
        }
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<Review> listByProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    private void requirePurchase(Long userId, Long productId) {
        PurchaseEntitlement entitlement = purchaseEntitlementRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new BusinessException("Only buyers of the product can review it"));
    }

    private void validateRating(short rating) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException("Rating must be between 1 and 5");
        }
    }
}