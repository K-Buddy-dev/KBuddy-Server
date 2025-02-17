package com.example.kbuddy_backend.qna.service;

import com.example.kbuddy_backend.common.IntegrationTest;
import com.example.kbuddy_backend.common.config.DataInitializer;
import com.example.kbuddy_backend.fixtures.UserFixtures;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.entity.QnaHeart;
import com.example.kbuddy_backend.qna.exception.DuplicatedQnaHeartException;
import com.example.kbuddy_backend.qna.exception.QnaHeartNotFoundException;
import com.example.kbuddy_backend.qna.repository.QnaHeartRepository;
import com.example.kbuddy_backend.qna.repository.QnaRepository;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class QnaServiceTest extends IntegrationTest {

    @Autowired
    private QnaService qnaService;

    @MockBean
    private QnaRepository qnaRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DataInitializer dataInitializer;

    @MockBean
    private QnaHeartRepository qnaHeartRepository;

    private static final long userId = 1L;

    private User user;
    private Long qnaId;
    private Qna qna;

    @BeforeEach
    void setUp() {
        user = UserFixtures.createUser();
        qnaId = 1L;
        qna = Qna.builder().build();
    }

    @DisplayName("좋아요 추가 테스트 - 이미 좋아요를 눌렀으면 예외 발생")
    @Test
    public void testPlusHeart_AlreadyLiked() {
        // given
        QnaHeart existingQnaHeart = new QnaHeart(user,qna); // 이미 존재하는 좋아요

        given(qnaHeartRepository.findByQnaIdAndUserId(any(), any()))
                .willReturn(Optional.of(existingQnaHeart));

        // when & then
        assertThrows(DuplicatedQnaHeartException.class, () -> {
            qnaService.plusHeart(qnaId, user);  // 이미 좋아요를 눌렀으면 예외 발생
        });

        verify(qnaHeartRepository, times(0)).save(any(QnaHeart.class));  // save는 호출되지 않아야 함
    }

    @DisplayName("좋아요 추가 테스트 - 좋아요를 처음 누를 때 정상적으로 저장")
    @Test
    public void testPlusHeart_FirstTimeLike() {
        // given
        given(qnaHeartRepository.findByQnaIdAndUserId(any(), any()))
                .willReturn(Optional.empty());  // 이미 좋아요를 누르지 않았음
        given(qnaRepository.findById(qnaId)).willReturn(Optional.of(qna));

        // when
        qnaService.plusHeart(qnaId, user);

        // then
        verify(qnaHeartRepository, times(1)).save(any(QnaHeart.class));  // 좋아요 저장이 호출되었는지 검증
    }

    @DisplayName("좋아요 취소 테스트 - 좋아요를 하지 않은 상태에서 취소 시 예외 발생")
    @Test
    public void testMinusHeart_NoLike() {
        // given
        given(qnaRepository.findById(qnaId))
                .willReturn(Optional.of(qna));
        given(qnaHeartRepository.findByQnaIdAndUserId(any(), any()))
                .willReturn(Optional.empty());  // 좋아요를 누르지 않음

        // when & then
        assertThrows(QnaHeartNotFoundException.class, () -> {
            qnaService.minusHeart(qnaId, user);  // 좋아요가 없으면 예외 발생
        });

        verify(qnaHeartRepository, times(0)).deleteByQnaIdAndUserId(qnaId, userId);  // 삭제는 호출되지 않아야 함
    }

    @DisplayName("좋아요 취소 테스트 - 좋아요를 눌렀을 때 정상적으로 취소")
    @Test
    public void testMinusHeart_LikeExists() {
        // given
        QnaHeart qnaHeart = new QnaHeart(user, qna);

        given(qnaHeartRepository.findByQnaIdAndUserId(any(), any()))
                .willReturn(Optional.of(qnaHeart));  // 이미 좋아요를 눌렀음
        given(qnaRepository.findById(any())).willReturn(Optional.of(qna));

        // when
        qnaService.minusHeart(qnaId, user);

        // then
        verify(qnaHeartRepository, times(1)).deleteByQnaIdAndUserId(any(), any());  // 삭제가 호출되었는지 검증
    }
}