package com.example.kbuddy_backend.qna.service;

import com.example.kbuddy_backend.common.constant.ImageFileType;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.qna.constant.SortBy;
import com.example.kbuddy_backend.qna.constant.QnaStatus;
import com.example.kbuddy_backend.qna.dto.request.QnaSaveRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaUpdateRequest;
import com.example.kbuddy_backend.qna.dto.response.AllQnaResponse;
import com.example.kbuddy_backend.qna.dto.response.QnaCommentResponse;
import com.example.kbuddy_backend.qna.dto.response.QnaPaginationResponse;
import com.example.kbuddy_backend.qna.dto.response.QnaResponse;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.entity.QnaBookmark;
import com.example.kbuddy_backend.qna.entity.QnaHeart;
import com.example.kbuddy_backend.qna.entity.QnaImage;
import com.example.kbuddy_backend.qna.exception.*;
import com.example.kbuddy_backend.qna.repository.*;
import com.example.kbuddy_backend.s3.dto.response.S3Response;
import com.example.kbuddy_backend.s3.service.S3Service;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.exception.UserNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.example.kbuddy_backend.common.exception.BadRequestException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QnaService {

    private static final int MAX_QNA_IMAGES = 5;

    private final QnaRepository qnaRepository;
    private final QnaHeartRepository qnaHeartRepository;
    private final QnaBookmarkRepository qnaBookmarkRepository;
    private final QnaImageRepository qnaImageRepository;
    private final S3Service s3Service;

    @Transactional
    public QnaResponse saveQna(QnaSaveRequest qnaSaveRequest, List<MultipartFile> imageFiles, User user) {
        if (imageFiles != null && imageFiles.size() > MAX_QNA_IMAGES) {
            throw new BadRequestException("Q&A 게시글에는 이미지를 최대 " + MAX_QNA_IMAGES + "개까지 첨부할 수 있습니다.");
        }

        String hashTag = "";
        if (qnaSaveRequest.hashtags() != null && !qnaSaveRequest.hashtags().isEmpty()) {
            hashTag = String.join(",", qnaSaveRequest.hashtags());
        }

        Qna qna = Qna.builder()
                .title(qnaSaveRequest.title())
                .description(qnaSaveRequest.description())
                .category(qnaSaveRequest.categoryId())
                .hashtag(hashTag)
                .writer(user)
                .status(qnaSaveRequest.status())
                .build();

        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<ImageFileDto> uploadedImages = uploadImages(imageFiles);
            saveImageFiles(uploadedImages, qna);
        }

        Qna saveQna = qnaRepository.save(qna);
        return createQnaResponseDto(saveQna, user);
    }

    /**
     * 이미지 파일들을 S3에 업로드하고 ImageFileDto 리스트를 반환합니다.
     */
    private List<ImageFileDto> uploadImages(List<MultipartFile> imageFiles) {
        List<ImageFileDto> uploadedImages = new ArrayList<>();

        for (MultipartFile file : imageFiles) {
            // 이미지 파일 확인
            if (!s3Service.checkImageFile(file)) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
            }

            // S3에 파일 업로드
            S3Response s3Response = s3Service.saveFileWithUUID(file, "qna");

            // 파일 타입 결정 (확장자 기반)
            String contentType = file.getContentType();
            ImageFileType fileType = contentType != null && contentType.contains("png")
                    ? ImageFileType.PNG : ImageFileType.JPEG;

            // ImageFileDto 생성 및 리스트에 추가
            uploadedImages.add(new ImageFileDto(0L,
                    fileType,
                    s3Response.filePath(),
                    s3Response.s3ImageUrl()
            ));
        }

        return uploadedImages;
    }

    public AllQnaResponse getAllQna(int pageSize, Long qnaId, String title, SortBy sortBy, Integer categoryCode, User currentUser) {
        List<Qna> allQna = qnaRepository.paginationNoOffset(qnaId, title, pageSize, sortBy, categoryCode, QnaStatus.PUBLISHED);
        List<QnaPaginationResponse> qnaPaginationResponseList = allQna.stream()
                .map(qna -> {
                    boolean isBookmarked = currentUser != null && qnaBookmarkRepository.existsByQnaIdAndUserId(qna.getId(), currentUser.getId());
                    boolean isHearted = currentUser != null && qnaHeartRepository.existsByQnaIdAndUserId(qna.getId(), currentUser.getId());
                    return QnaPaginationResponse.of(qna.getId(), qna.getWriter().getId(), qna.getCategoryCode(),
                            qna.getTitle(), qna.getDescription(), qna.getViewCount(), qna.getHeartCount(),
                            qna.getCommentCount(), qna.getCreatedDate(),
                            qna.getLastModifiedDate(), qna.getStatus(), isBookmarked, isHearted);
                })
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
    public QnaResponse getQna(Long qnaId, User currentUser) {
        Qna qnaById = findQnaById(qnaId);

        if (qnaById.getStatus() == QnaStatus.DRAFT) {
            if (currentUser == null) {
                throw new AccessDeniedException("로그인이 필요합니다.");
            }
            if (!Objects.equals(qnaById.getWriter().getId(), currentUser.getId())) {
                throw new AccessDeniedException("임시 저장된 글은 작성자만 조회할 수 있습니다."); // Or use NotWriterException
            }
        }
        qnaById.plusViewCount();
        return createQnaResponseDto(qnaById, currentUser);
    }

    @Transactional
    public QnaResponse updateQna(Long qnaId, QnaUpdateRequest qnaUpdateRequest, List<MultipartFile> newFiles, User user) {
        Qna qnaById = findQnaById(qnaId);
        isQnaWriter(user, qnaById);

        int currentImageCount = qnaById.getImageUrls().size();
        int deletedImageCount = (qnaUpdateRequest.deleteImageIds() != null) ? qnaUpdateRequest.deleteImageIds().size() : 0;
        int newImageCount = (newFiles != null) ? newFiles.size() : 0;
        int finalImageCount = currentImageCount - deletedImageCount + newImageCount;

        if (finalImageCount > MAX_QNA_IMAGES) {
            throw new BadRequestException("Q&A 게시글에는 이미지를 최대 " + MAX_QNA_IMAGES + "개까지 첨부할 수 있습니다. (현재 " + finalImageCount + "개)");
        }

        String hashTag = "";
        if (qnaUpdateRequest.hashtags() != null && !qnaUpdateRequest.hashtags().isEmpty()) {
            hashTag = String.join(",", qnaUpdateRequest.hashtags());
        }

        if (qnaUpdateRequest.deleteImageIds() != null && !qnaUpdateRequest.deleteImageIds().isEmpty()) {
            deleteImages(qnaId, qnaUpdateRequest.deleteImageIds(), user);
        }

        if (newFiles != null && !newFiles.isEmpty()) {
            List<ImageFileDto> uploadedImages = uploadImages(newFiles);
            saveImageFiles(uploadedImages, qnaById);
        }

        qnaById.update(qnaUpdateRequest.title(), qnaUpdateRequest.description(), hashTag,
                qnaUpdateRequest.categoryId(), qnaUpdateRequest.status());
        return createQnaResponseDto(qnaById, user);
    }

    private static void isQnaWriter(User user, Qna qnaById) {
        if (!Objects.equals(qnaById.getWriter().getId(), user.getId())) {
            throw new NotWriterException();
        }
    }

    @Transactional
    public void deleteImages(Long qnaId, List<Long> imageIds, User user) {
        Qna qna = findQnaById(qnaId);
        isQnaWriter(user, qna);
        for (Long imageId : imageIds) {
            qna.deleteImage(imageId);
            QnaImage qnaImage = qnaImageRepository.findById(imageId).orElseThrow(QnaImageNotFoundException::new);
            // S3에서도 이미지 삭제
            s3Service.deleteFile(qnaImage.getImageUrl());
        }
    }

    @Transactional
    public void deleteQna(Long qnaId, User user) {
        Qna qna = qnaRepository.findById(qnaId).orElseThrow(QnaNotFoundException::new);

        //해당 게시글 작성자가 아닐 경우
        isQnaWriter(user, qna);

        // 게시글에 첨부된 이미지들도 S3에서 삭제
        List<QnaImage> images = qna.getImageUrls();
        for (QnaImage image : images) {
            s3Service.deleteFile(image.getFilePath());
        }
        qnaRepository.delete(qna);
    }

    private QnaResponse createQnaResponseDto(Qna qna, User currentUser) {
        List<ImageFileDto> images = qna.getImageUrls()
                .stream()
                .map(qnaImage -> new ImageFileDto(qnaImage.getId(), qnaImage.getFileType(), qnaImage.getFilePath(),
                        qnaImage.getImageUrl()))
                .toList();

        boolean isBookmarked = qnaBookmarkRepository.existsByQnaIdAndUserId(qna.getId(), currentUser.getId());
        boolean isHearted = qnaHeartRepository.existsByQnaIdAndUserId(qna.getId(), currentUser.getId());

        List<QnaCommentResponse> comments = qna.getComments()
                .stream()
                .map(qnaComment -> QnaCommentResponse.of(qnaComment.getId(), qnaComment.getQna().getId(),
                        qnaComment.getWriter().getId(), qnaComment.getContent(), qnaComment.getCreatedDate(),
                        qnaComment.getLastModifiedDate()))
                .sorted(Comparator.comparing(QnaCommentResponse::createdAt))
                .toList();

        return QnaResponse.of(qna.getId(), qna.getWriter().getId(), qna.getCategoryCode(), qna.getTitle(),
                qna.getDescription(), qna.getViewCount(), qna.getCreatedDate(), qna.getLastModifiedDate(),
                images, comments, qna.getHeartCount(), qna.getCommentCount(), isBookmarked, isHearted,
                qna.getStatus());
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
    public void addBookmark(User user, Long qnaId) {
        QnaBookmark qnaBookmark = new QnaBookmark(findQnaById(qnaId), user);
        qnaBookmarkRepository.save(qnaBookmark);
    }

    @Transactional
    public void removeBookmark(User user, Long qnaId) {
        QnaBookmark qnaBookmark = qnaBookmarkRepository.findByQnaIdAndUserId(qnaId, user.getId())
                .orElseThrow(QnaBookmarkNotFoundException::new);
        qnaBookmarkRepository.delete(qnaBookmark);
    }

    public Qna findQnaById(Long qnaId) {
        return qnaRepository.findById(qnaId).orElseThrow(QnaNotFoundException::new);
    }

    public List<QnaPaginationResponse> getMyDrafts(User currentUser) {

        List<Qna> draftQnas = qnaRepository.findByWriterAndStatus(currentUser, QnaStatus.DRAFT);
        return draftQnas.stream()
                .map(qna -> {
                    boolean isBookmarked = qnaBookmarkRepository.existsByQnaIdAndUserId(qna.getId(), currentUser.getId());
                    boolean isHearted = qnaHeartRepository.existsByQnaIdAndUserId(qna.getId(), currentUser.getId());
                    return QnaPaginationResponse.of(
                            qna.getId(),
                            qna.getWriter().getId(),
                            qna.getCategoryCode(),
                            qna.getTitle(),
                            qna.getDescription(),
                            qna.getViewCount(),
                            qna.getHeartCount(),
                            qna.getCommentCount(),
                            qna.getCreatedDate(),
                            qna.getLastModifiedDate(),
                            qna.getStatus(),
                            isBookmarked,
                            isHearted
                    );
                })
                .collect(Collectors.toList()); // Collect results into a List
    }

}