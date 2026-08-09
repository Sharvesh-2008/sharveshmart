package com.digitalmarketplace.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        Long categoryId,

        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @Size(max = 10000, message = "Description must be at most 10000 characters")
        String description,

        @DecimalMin(value = "0.00", message = "Price must not be negative")
        @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer and 2 fraction digits")
        BigDecimal price
) {
}
