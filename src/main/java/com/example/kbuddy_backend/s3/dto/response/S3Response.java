package com.example.kbuddy_backend.s3.dto.response;

public record S3Response (
        String originalFileName,
        String filePath,
        String s3ImageUrl
) {

    public static S3Response of (String originalFileName, String filePath, String s3ImageUrl) {
        return new S3Response(originalFileName, filePath, s3ImageUrl);
    }
}
