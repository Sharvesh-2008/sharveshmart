package com.digitalmarketplace.controller;

import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.Review;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.exception.ResourceNotFoundException;
import com.digitalmarketplace.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    private User user() {
        User user = new User();
        user.setId(2L);
        user.setName("Buyer");
        return user;
    }

    private Product product() {
        Product product = new Product();
        product.setId(3L);
        product.setTitle("Spring Guide");
        return product;
    }

    private Review review() {
        Review review = new Review();
        review.setId(1L);
        review.setUser(user());
        review.setProduct(product());
        review.setRating((short) 5);
        review.setComment("Great book");
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }

    @Test
    void listReviewsReturnsReviews() throws Exception {
        when(reviewService.listByProduct(3L)).thenReturn(List.of(review()));

        mockMvc.perform(get("/api/products/3/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productId").value(3))
                .andExpect(jsonPath("$[0].userName").value("Buyer"))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void createReviewReturnsCreated() throws Exception {
        when(reviewService.createReview(2L, 3L, (short) 5, "Great book")).thenReturn(review());

        mockMvc.perform(post("/api/products/3/reviews")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 5,
                                  "comment": "Great book"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void createReviewWithInvalidRatingReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/products/3/reviews")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 6
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReviewWithoutUserHeaderReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/products/3/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 5
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReviewWhenNotBuyerReturnsBadRequest() throws Exception {
        when(reviewService.createReview(2L, 3L, (short) 5, "Great book"))
                .thenThrow(new BusinessException("Only buyers of the product can review it"));

        mockMvc.perform(post("/api/products/3/reviews")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 5,
                                  "comment": "Great book"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateReviewReturnsUpdated() throws Exception {
        when(reviewService.updateReview(2L, 1L, (short) 4, "Good")).thenReturn(review());

        mockMvc.perform(put("/api/reviews/1")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 4,
                                  "comment": "Good"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateReviewWhenNotFoundReturnsNotFound() throws Exception {
        when(reviewService.updateReview(2L, 99L, (short) 4, "Good"))
                .thenThrow(new ResourceNotFoundException("Review not found"));

        mockMvc.perform(put("/api/reviews/99")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 4,
                                  "comment": "Good"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReviewReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/reviews/1").header("X-User-Id", "2"))
                .andExpect(status().isNoContent());
    }
}
