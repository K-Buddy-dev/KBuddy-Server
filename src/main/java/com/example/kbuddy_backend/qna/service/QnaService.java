package com.example.kbuddy_backend.qna.service;

import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.qna.constant.SortBy;
import com.example.kbuddy_backend.qna.dto.request.BookmarkRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaImageRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaSaveRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaUpdateRequest;
import com.example.kbuddy_backend.qna.dto.response.AllQnaResponse;
import com.example.kbuddy_backend.qna.dto.response.QnaCommentResponse;
import com.example.kbuddy_backend.qna.dto.response.QnaPaginationResponse;
import com.example.kbuddy_backend.qna.dto.response.QnaResponse;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.entity.QnaBookmark;
import com.example.kbuddy_backend.qna.entity.QnaCategory;
import com.example.kbuddy_backend.qna.entity.QnaCollection;
import com.example.kbuddy_backend.qna.entity.QnaHeart;
import com.example.kbuddy_backend.qna.entity.QnaImage;
import com.example.kbuddy_backend.qna.exception.DuplicatedQnaHeartException;
import com.example.kbuddy_backend.qna.exception.NotWriterException;
import com.example.kbuddy_backend.qna.exception.QnaHeartNotFoundException;
import com.example.kbuddy_backend.qna.exception.QnaNotFoundException;
import com.example.kbuddy_backend.qna.repository.QnaBookmarkRepository;
import com.example.kbuddy_backend.qna.repository.QnaCategoryRepository;
import com.example.kbuddy_backend.qna.repository.QnaCollectionRepository;
import com.example.kbuddy_backend.qna.repository.QnaHeartRepository;
import com.example.kbuddy_backend.qna.repository.QnaRepository;
import com.example.kbuddy_backend.s3.service.S3Service;
import com.example.kbuddy_backend.user.entity.User;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QnaService {

    private final QnaRepository qnaRepository;
    private final QnaHeartRepository qnaHeartRepository;
    private final QnaCategoryRepository qnaCategoryRepository;
    private final QnaCollectionRepository qnaCollectionRepository;
    private final QnaBookmarkRepository qnaBookmarkRepository;

    @Transactional
    public QnaResponse saveQna(QnaSaveRequest qnaSaveRequest, User user) {

        List<ImageFileDto> imageFiles = qnaSaveRequest.file();

        String hashtag = String.join(",", qnaSaveRequest.hashtags());
        QnaCategory qnaCategory = findCategoryById(qnaSaveRequest.categoryId());

        Qna qna = Qna.builder()
                .title(qnaSaveRequest.title())
                .description(qnaSaveRequest.description())
                .category(qnaCategory)
                .hashtag(hashtag)
                .writer(user)
                .build();

        //todo: 트랜잭션 실패 시 이미지 삭제하는 로직 추가
        if (imageFiles != null) {
            saveImageFiles(imageFiles, qna);
        }

        Qna saveQna = qnaRepository.save(qna);
        return createQnaResponseDto(saveQna);
    }

    public AllQnaResponse getAllQna(int pageSize, Long qnaId, String title, SortBy sortBy) {
        List<Qna> allQna = qnaRepository.paginationNoOffset(qnaId, title, pageSize, sortBy);
        List<QnaPaginationResponse> qnaPaginationResponseList = allQna.stream()
                .map(qna -> QnaPaginationResponse.of(qna.getId(), qna.getWriter().getId(), qna.getCategory().getId(),
                        qna.getTitle(), qna.getDescription(), qna.getViewCount(), qna.getHeartCount(),
                        qna.getCommentCount(), qna.getCreatedDate(),
                        qna.getLastModifiedDate()))
                .toList();

        Long nextId = getNextId(qnaPaginationResponseList);

        return AllQnaResponse.of(nextId, qnaPaginationResponseList);
    }

    private Long getNextId(List<QnaPaginationResponse> responses) {
        return responses.stream()
                .map(QnaPaginationResponse::id)
                .reduce((first, second) -> second) // 마지막 ID를 반환
                .orElse(-1L); // 리스트가 비어있으면 -1 반환
    }

    @Transactional
    public QnaResponse getQna(Long qnaId) {
        Qna qnaById = findQnaById(qnaId);
        qnaById.plusViewCount();
        return createQnaResponseDto(qnaById);
    }

    @Transactional
    public QnaResponse updateQna(Long qnaId, QnaUpdateRequest qnaUpdateRequest, User user) {

        Qna qnaById = findQnaById(qnaId);

        isQnaWriter(user, qnaById);

        String hashtag = String.join(",", qnaUpdateRequest.hashtags());
        QnaCategory categoryById = findCategoryById(qnaUpdateRequest.categoryId());
        qnaById.update(qnaUpdateRequest.title(), qnaUpdateRequest.description(), hashtag, categoryById);
        return createQnaResponseDto(qnaById);
    }

    private static void isQnaWriter(User user, Qna qnaById) {
        if (!Objects.equals(qnaById.getWriter().getId(), user.getId())) {
            throw new NotWriterException();
        }
    }

    @Transactional
    public void addImages(Long qnaId, List<ImageFileDto> images, User user) {
        Qna qna = findQnaById(qnaId);
        isQnaWriter(user, qna);
        saveImageFiles(images, qna);
    }

    @Transactional
    public void deleteImages(Long qnaId, List<ImageFileDto> images, User user) {
        Qna qna = findQnaById(qnaId);
        isQnaWriter(user, qna);
        for (ImageFileDto image : images) {
            qna.deleteImage(image.name());
        }
    }

    @Transactional
    public void deleteQna(Long qnaId, User user) {
        Qna qna = qnaRepository.findById(qnaId).orElseThrow(QnaNotFoundException::new);

        //해당 게시글 작성자가 아닐 경우
        isQnaWriter(user, qna);
        qnaRepository.delete(qna);
    }

    private QnaResponse createQnaResponseDto(Qna qna) {
        List<ImageFileDto> images = qna.getImageUrls()
                .stream()
                .map(qnaImage -> new ImageFileDto(qnaImage.getFileType(), qnaImage.getFilePath(),
                        qnaImage.getImageUrl()
                ))
                .toList();
        List<QnaCommentResponse> comments = qna.getComments()
                .stream()
                .map(qnaComment ->
                        QnaCommentResponse.of(qnaComment.getId(), qnaComment.getQna().getId(),
                                qnaComment.getWriter().getId(),
                                qnaComment.getContent(), qnaComment.getCreatedDate(),
                                qnaComment.getLastModifiedDate()))
                .sorted(Comparator.comparing(QnaCommentResponse::createdAt))
                .toList();

        return QnaResponse.of(qna.getId(), qna.getWriter().getId(), qna.getCategory().getId(), qna.getTitle(),
                qna.getDescription(), qna.getViewCount(), qna.getCreatedDate(), qna.getLastModifiedDate(),
                images, comments, qna.getHeartCount(), qna.getCommentCount());
    }

    private void saveImageFiles(List<ImageFileDto> imageFiles, Qna qna) {
        for (ImageFileDto imageFile : imageFiles) {
            QnaImage qnaImage = QnaImage.builder()
                    .qna(qna)
                    .imageUrl(imageFile.url())
                    .filePath(imageFile.name())
                    .fileType(imageFile.type())
                    .build();
            qna.addImage(qnaImage);
        }
    }

    @Transactional
    public void plusHeart(Long qnaId, User user) {
        qnaHeartRepository.findByQnaIdAndUserId(qnaId, user.getId())
                .ifPresent(qnaHeart -> {
                    throw new DuplicatedQnaHeartException();
                });
        Qna qna = findQnaById(qnaId);
        QnaHeart qnaHeart = new QnaHeart(user, qna);
        qna.plusHeart(qnaHeart);
        qnaHeartRepository.save(qnaHeart);
    }

    @Transactional
    public void minusHeart(Long qnaId, User user) {

        Qna qnaById = findQnaById(qnaId);
        QnaHeart byQnaIdAndUserId = qnaHeartRepository.findByQnaIdAndUserId(qnaId, user.getId())
                .orElseThrow(QnaHeartNotFoundException::new);
        qnaById.minusHeart(byQnaIdAndUserId);
        qnaHeartRepository.deleteByQnaIdAndUserId(qnaId, user.getId());
    }

    @Transactional
    public void addBookmark(BookmarkRequest bookmarkRequest, Long qnaId) {
        QnaCollection collectionById = findCollectionById(bookmarkRequest.collectionId());
        QnaBookmark qnaBookmark = new QnaBookmark(findQnaById(qnaId), collectionById);
        collectionById.addBookmark(qnaBookmark);
    }


    @Transactional
    public void removeBookmark(BookmarkRequest bookmarkRequest, Long qnaId) {
        QnaCollection collectionById = findCollectionById(bookmarkRequest.collectionId());
        QnaBookmark qnaBookmark = qnaBookmarkRepository.findByQna(findQnaById(qnaId))
                .orElseThrow(() -> new IllegalArgumentException("북마크 된 QnA가 아닙니다."));
        collectionById.removeBookmark(qnaBookmark);
    }

    public Qna findQnaById(Long qnaId) {
        return qnaRepository.findById(qnaId).orElseThrow(QnaNotFoundException::new);
    }

    private QnaCategory findCategoryById(Long categoryId) {
        return qnaCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
    }

    public QnaCollection findCollectionById(Long bookmarkId) {
        return qnaCollectionRepository.findById(bookmarkId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 컬렉션입니다."));
    }


    //todo: commentCount 수정

}