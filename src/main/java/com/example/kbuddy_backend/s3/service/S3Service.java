package com.example.kbuddy_backend.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.kbuddy_backend.s3.dto.response.S3Response;
import com.example.kbuddy_backend.s3.exception.ImageUploadException;
import java.io.IOException;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    private final AmazonS3 amazonS3;

    /*
    * 버킷 변경 필요
    * */
    @Getter
    @Value("${cloud.naver.s3.bucket}")
    private String bucketName;

    public boolean checkImageFile(MultipartFile multipartFile) {
        String contentType = multipartFile.getContentType();
        return contentType != null && contentType.startsWith("image");
    }

    public S3Response saveFileWithUUID(MultipartFile multipartFile, String folderName) {
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            ObjectMetadata metadata = setMetadata(multipartFile);

            String uuid = UUID.randomUUID().toString();
            String filePath = folderName + "/" + uuid + extension;
            saveImageWithUUID(multipartFile, metadata, filePath);
            //이미지 URL 전체공개
            amazonS3.setObjectAcl(bucketName, filePath, CannedAccessControlList.PublicRead);
            return
                    S3Response.of(originalFilename, filePath, amazonS3.getUrl(bucketName, filePath).toString());
        } catch (IOException e) {
            log.error("s3에 file 저장중 에러 발생", e);
            throw new ImageUploadException(e.getMessage());
        }
    }

    private void saveImageWithUUID(MultipartFile multipartFile, ObjectMetadata metadata, String filePath)
            throws IOException {
        amazonS3.putObject(bucketName, filePath, multipartFile.getInputStream(), metadata);
    }

    public void deleteFile(String objectName) {
        amazonS3.deleteObject(bucketName, objectName);
    }

    private ObjectMetadata setMetadata(MultipartFile multipartFile) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        return metadata;
    }
}
