package com.example.kbuddy_backend.fixtures;

import com.example.kbuddy_backend.common.constant.ImageFileType;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.qna.dto.request.QnaSaveRequest;
import com.example.kbuddy_backend.qna.dto.response.QnaResponse;
import com.example.kbuddy_backend.qna.entity.Qna;
import java.time.LocalDateTime;
import java.util.List;

public class QnaFixtures {

    public static QnaSaveRequest createQnaSaveRequest() {
        return QnaSaveRequest.of("title", "description", List.of(ImageFileDto.of(ImageFileType.PNG,"test_pic","test_url")), List.of("cafe","theater"), 1L);
    }

    public static QnaResponse createQnaResponse() {
        return QnaResponse.of(1L, 123L, 1L, "title", "description", 0, LocalDateTime.now(), LocalDateTime.now(), List.of(ImageFileDto.of(ImageFileType.PNG,"test_pic","test_url")), null, 0, 0);
    }
}
