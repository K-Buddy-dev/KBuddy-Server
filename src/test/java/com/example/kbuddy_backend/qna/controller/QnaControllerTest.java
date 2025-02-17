package com.example.kbuddy_backend.qna.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.kbuddy_backend.common.WebMVCTest;
import com.example.kbuddy_backend.fixtures.DefaultMessageFixtures;
import com.example.kbuddy_backend.fixtures.QnaFixtures;
import com.example.kbuddy_backend.fixtures.UserFixtures;
import com.example.kbuddy_backend.qna.dto.request.QnaSaveRequest;
import com.example.kbuddy_backend.qna.dto.response.QnaResponse;
import com.example.kbuddy_backend.qna.service.QnaService;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;
import com.example.kbuddy_backend.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser
public class QnaControllerTest extends WebMVCTest {

    @MockBean
    private QnaService qnaService;

    @DisplayName("QnA 게시글 작성 테스트")
    @Test
    public void testCreateQnA() throws Exception {

        //given
        QnaSaveRequest qnaSaveRequest = QnaFixtures.createQnaSaveRequest();
        QnaResponse qnaResponse = QnaFixtures.createQnaResponse();
        User user = UserFixtures.createUser();

        given(qnaService.saveQna(any(QnaSaveRequest.class), eq(user))).willReturn(qnaResponse);
        given(userRepository.findByEmail(any())).willReturn(Optional.of(user));

        //when
        mockMvc.perform(post("/kbuddy/v1/qna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qnaSaveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(true))
                .andExpect(jsonPath("$.data.message").value("게시글 작성 성공"))
                .andDo(print());

        //then
        verify(qnaService, times(1)).saveQna(any(QnaSaveRequest.class),eq(user));
    }
}
