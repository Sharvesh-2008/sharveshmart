package com.sharveshmart.dto;

import com.sharveshmart.entity.ProductFile;

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
