package com.example.kbuddy_backend.qna.dto.request;

import com.example.kbuddy_backend.common.dto.ImageFileDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record QnaSaveRequest(Long categoryId, String title, String description, List<ImageFileDto> file, List<String> hashtags) {
    public static QnaSaveRequest of(String title, String description, List<ImageFileDto> file,List<String> hashtags, Long categoryId) {
        return new QnaSaveRequest(categoryId,title, description,file, hashtags);
    }
}
