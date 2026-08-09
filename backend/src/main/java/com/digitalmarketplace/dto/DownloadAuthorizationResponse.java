package com.digitalmarketplace.dto;

import com.digitalmarketplace.entity.ProductFile;

public record DownloadAuthorizationResponse(
        Long productId,
        String productTitle,
        String fileName,
        String fileType,
        long fileSize
) {

    public static DownloadAuthorizationResponse from(ProductFile file) {
        return new DownloadAuthorizationResponse(
                file.getProduct().getId(),
                file.getProduct().getTitle(),
                file.getFileName(),
                file.getFileType(),
                file.getFileSize()
        );
    }
}
