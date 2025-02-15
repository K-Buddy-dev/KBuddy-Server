// QnaServiceTest.java
package com.example.kbuddy_backend.qna.service;

import com.example.kbuddy_backend.qna.dto.request.QnaSaveRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaUpdateRequest;
import com.example.kbuddy_backend.qna.dto.response.QnaResponse;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.entity.QnaCategory;
import com.example.kbuddy_backend.qna.repository.QnaBookmarkRepository;
import com.example.kbuddy_backend.qna.repository.QnaCategoryRepository;
import com.example.kbuddy_backend.qna.repository.QnaCollectionRepository;
import com.example.kbuddy_backend.qna.repository.QnaHeartRepository;
import com.example.kbuddy_backend.qna.repository.QnaRepository;
import com.example.kbuddy_backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QnaServiceTest {

    @Mock
    private QnaRepository qnaRepository;

    @Mock
    private QnaHeartRepository qnaHeartRepository;

    @Mock
    private QnaCategoryRepository qnaCategoryRepository;

    @Mock
    private QnaCollectionRepository qnaCollectionRepository;

    @Mock
    private QnaBookmarkRepository qnaBookmarkRepository;

    @InjectMocks
    private QnaService qnaService;

    @Test
    public void testSaveQna() {
        // Arrange
        QnaSaveRequest request = mock(QnaSaveRequest.class);
        User user = mock(User.class);
        QnaCategory category = mock(QnaCategory.class);
        when(qnaCategoryRepository.findById(any())).thenReturn(Optional.of(category));
        when(qnaRepository.save(any(Qna.class))).thenReturn(mock(Qna.class));

        // Act
        QnaResponse response = qnaService.saveQna(request, user);

        // Assert
        assertNotNull(response);
        verify(qnaRepository, times(1)).save(any(Qna.class));
    }

    @Test
    public void testGetQna() {
        // Arrange
        Qna qna = mock(Qna.class);
        when(qnaRepository.findById(anyLong())).thenReturn(Optional.of(qna));

        // Act
        QnaResponse response = qnaService.getQna(1L);

        // Assert
        assertNotNull(response);
        verify(qnaRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testUpdateQna() {
        // Arrange
        Qna qna = mock(Qna.class);
        User user = mock(User.class);
        QnaUpdateRequest request = mock(QnaUpdateRequest.class);
        QnaCategory category = mock(QnaCategory.class);
        when(qnaRepository.findById(anyLong())).thenReturn(Optional.of(qna));
        when(qnaCategoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        // Act
        QnaResponse response = qnaService.updateQna(1L, request, user);

        // Assert
        assertNotNull(response);
        verify(qnaRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testDeleteQna() {
        // Arrange
        Qna qna = mock(Qna.class);
        User user = mock(User.class);
        when(qnaRepository.findById(anyLong())).thenReturn(Optional.of(qna));

        // Act
        qnaService.deleteQna(1L, user);

        // Assert
        verify(qnaRepository, times(1)).delete(any(Qna.class));
    }
}