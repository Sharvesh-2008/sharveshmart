package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.PurchaseEntitlement;
import com.digitalmarketplace.entity.Review;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.repository.ProductRepository;
import com.digitalmarketplace.repository.PurchaseEntitlementRepository;
import com.digitalmarketplace.repository.ReviewRepository;
import com.digitalmarketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PurchaseEntitlementRepository purchaseEntitlementRepository;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, userRepository,
                productRepository, purchaseEntitlementRepository);
    }

    private User user(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Product product(long id) {
        Product product = new Product();
        product.setId(id);
        return product;
    }

    private Review review(long id, long authorId) {
        Review review = new Review();
        review.setId(id);
        review.setUser(user(authorId));
        return review;
    }

    @Test
    void createReviewSavesWhenBuyerHasAccessAndNoReviewExists() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product(10L)));
        when(reviewRepository.findByUserIdAndProductId(5L, 10L)).thenReturn(Optional.empty());
        when(purchaseEntitlementRepository.findByUserIdAndProductId(5L, 10L))
                .thenReturn(Optional.of(new PurchaseEntitlement()));
        when(reviewRepository.save(any(Review.class))).then(returnsFirstArg());

        Review result = reviewService.createReview(5L, 10L, (short) 4, "Great");

        assertEquals(4, result.getRating());
        assertEquals("Great", result.getComment());
        assertEquals(5L, result.getUser().getId());
        assertEquals(10L, result.getProduct().getId());
    }

    @Test
    void createReviewThrowsWhenRatingIsBelowOne() {
        assertThrows(BusinessException.class,
                () -> reviewService.createReview(5L, 10L, (short) 0, "Bad"));
    }

    @Test
    void createReviewThrowsWhenRatingIsAboveFive() {
        assertThrows(BusinessException.class,
                () -> reviewService.createReview(5L, 10L, (short) 6, "Too high"));
    }

    @Test
    void createReviewThrowsWhenUserDidNotBuyTheProduct() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product(10L)));
        when(reviewRepository.findByUserIdAndProductId(5L, 10L)).thenReturn(Optional.empty());
        when(purchaseEntitlementRepository.findByUserIdAndProductId(5L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> reviewService.createReview(5L, 10L, (short) 4, "x"));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReviewThrowsWhenReviewAlreadyExists() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product(10L)));
        when(reviewRepository.findByUserIdAndProductId(5L, 10L))
                .thenReturn(Optional.of(review(1L, 5L)));

        assertThrows(BusinessException.class,
                () -> reviewService.createReview(5L, 10L, (short) 4, "x"));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void updateReviewAllowsOnlyTheAuthor() {
        Review review = review(1L, 5L);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(BusinessException.class,
                () -> reviewService.updateReview(9L, 1L, (short) 5, "x"));
    }

    @Test
    void updateReviewSetsNewRatingAndComment() {
        Review review = review(1L, 5L);
        review.setRating((short) 1);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).then(returnsFirstArg());

        Review result = reviewService.updateReview(5L, 1L, (short) 5, "Updated");

        assertEquals(5, result.getRating());
        assertEquals("Updated", result.getComment());
    }

    @Test
    void deleteReviewAllowsOnlyTheAuthor() {
        Review review = review(1L, 5L);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(BusinessException.class, () -> reviewService.deleteReview(9L, 1L));
    }

    @Test
    void deleteReviewRemovesOwnReview() {
        Review review = review(1L, 5L);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(5L, 1L);

        verify(reviewRepository).delete(review);
    }

    @Test
    void listByProductDelegatesToRepository() {
        when(reviewRepository.findByProductId(10L)).thenReturn(List.of(new Review()));

        assertEquals(1, reviewService.listByProduct(10L).size());
    }
}