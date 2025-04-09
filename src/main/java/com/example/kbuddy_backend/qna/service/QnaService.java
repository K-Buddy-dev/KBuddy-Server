package com.example.kbuddy_backend.qna.service;

import com.example.kbuddy_backend.common.constant.ImageFileType;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.qna.constant.SortBy;
import com.example.kbuddy_backend.qna.dto.request.BookmarkRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaSaveRequest;
import com.example.kbuddy_backend.qna.dto.request.QnaUpdateRequest;
import com.example.kbuddy_backend.qna.dto.response.AllQnaResponse;
import com.example.kbuddy_backend.qna.dto.response.QnaCommentResponse;
import com.example.kbuddy_backend.qna.dto.response.QnaPaginationResponse;
import com.example.kbuddy_backend.qna.dto.response.QnaResponse;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.entity.QnaBookmark;
import com.example.kbuddy_backend.qna.entity.QnaCollection;
import com.example.kbuddy_backend.qna.entity.QnaHeart;
import com.example.kbuddy_backend.qna.entity.QnaImage;
import com.example.kbuddy_backend.qna.exception.*;
import com.example.kbuddy_backend.qna.repository.*;
import com.example.kbuddy_backend.s3.dto.response.S3Response;
import com.example.kbuddy_backend.s3.service.S3Service;
import com.example.kbuddy_backend.user.entity.User;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
    private final QnaCollectionRepository qnaCollectionRepository;
    private final QnaBookmarkRepository qnaBookmarkRepository;
    private final QnaImageRepository qnaImageRepository;
    private final S3Service s3Service;

    @Transactional
    public QnaResponse saveQna(QnaSaveRequest qnaSaveRequest, List<MultipartFile> imageFiles, User user) {
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
                .build();

        // 이미지가 있으면 S3에 업로드하고 연결
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<ImageFileDto> uploadedImages = uploadImages(imageFiles);
            saveImageFiles(uploadedImages, qna);
        }

        Qna saveQna = qnaRepository.save(qna);
        return createQnaResponseDto(saveQna);
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
    
    public AllQnaResponse getAllQna(int pageSize, Long qnaId, String title, SortBy sortBy, Integer categoryCode) {
        List<Qna> allQna = qnaRepository.paginationNoOffset(qnaId, title, pageSize, sortBy, categoryCode);
        List<QnaPaginationResponse> qnaPaginationResponseList = allQna.stream()
                .map(qna -> QnaPaginationResponse.of(qna.getId(), qna.getWriter().getId(), qna.getCategoryCode(),
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
    public QnaResponse updateQna(Long qnaId, QnaUpdateRequest qnaUpdateRequest, List<MultipartFile> imageFiles, User user) {
        //추후에 해시태그 변경 로직 추가
        String hashTag = "";
        if (qnaUpdateRequest.hashtags() != null && !qnaUpdateRequest.hashtags().isEmpty()) {
            hashTag = String.join(",", qnaUpdateRequest.hashtags());
        }
        Qna qnaById = findQnaById(qnaId);

        // 이미지가 있으면 S3에 업로드하고 연결
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<ImageFileDto> uploadedImages = uploadImages(imageFiles);
            saveImageFiles(uploadedImages, qnaById);
        }
        // 기존 이미지 삭제
        if (!qnaUpdateRequest.deleteImageIds().isEmpty()) {
            deleteImages(qnaId,qnaUpdateRequest.deleteImageIds(), user);
        }
        isQnaWriter(user, qnaById);
        qnaById.update(qnaUpdateRequest.title(), qnaUpdateRequest.description(), hashTag, qnaUpdateRequest.categoryId());
        return createQnaResponseDto(qnaById);
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

    private QnaResponse createQnaResponseDto(Qna qna) {
        List<ImageFileDto> images = qna.getImageUrls()
                .stream()
                .map(qnaImage -> new ImageFileDto(qnaImage.getId(),qnaImage.getFileType(), qnaImage.getFilePath(),
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

        return QnaResponse.of(qna.getId(), qna.getWriter().getId(), qna.getCategoryCode(), qna.getTitle(),
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

    public QnaCollection findCollectionById(Long bookmarkId) {
        return qnaCollectionRepository.findById(bookmarkId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 컬렉션입니다."));
    }
}