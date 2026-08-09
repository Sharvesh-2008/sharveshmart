package com.digitalmarketplace.controller;

import com.digitalmarketplace.dto.DownloadAuthorizationResponse;
import com.digitalmarketplace.dto.LibraryItemResponse;
import com.digitalmarketplace.entity.ProductFile;
import com.digitalmarketplace.exception.ForbiddenException;
import com.digitalmarketplace.service.PurchaseEntitlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/library")
@Validated
@Tag(name = "Digital Library", description = "Purchased products and download authorization")
public class DigitalLibraryController {

    private final PurchaseEntitlementService entitlementService;

    public DigitalLibraryController(PurchaseEntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    @GetMapping
    public List<LibraryItemResponse> listLibrary(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId) {
        return entitlementService.listForUser(userId).stream().map(LibraryItemResponse::from).toList();
    }

    @Operation(summary = "Get download authorization for a purchased product")
    @GetMapping("/products/{productId}/download")
    public DownloadAuthorizationResponse download(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId,
            @PathVariable @Positive(message = "Product id must be positive") Long productId) {
        ProductFile file = entitlementService.getAuthorizedFile(userId, productId)
                .orElseThrow(() -> new ForbiddenException("You do not have access to this product"));
        return DownloadAuthorizationResponse.from(file);
    }
}
