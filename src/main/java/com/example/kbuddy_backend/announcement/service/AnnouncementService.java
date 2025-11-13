package com.example.kbuddy_backend.announcement.service;

import com.example.kbuddy_backend.common.constant.ImageFileType;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.notification.service.FCMService;
import com.example.kbuddy_backend.announcement.constant.SortBy;
import com.example.kbuddy_backend.announcement.dto.request.*;
import com.example.kbuddy_backend.announcement.dto.response.*;
import com.example.kbuddy_backend.announcement.entity.Announcement;
import com.example.kbuddy_backend.announcement.entity.AnnouncementImage;
import com.example.kbuddy_backend.announcement.exception.*;
import com.example.kbuddy_backend.announcement.repository.*;
import com.example.kbuddy_backend.s3.dto.response.S3Response;
import com.example.kbuddy_backend.s3.service.S3Service;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.exception.UserNotFoundException;;
import com.example.kbuddy_backend.user.service.UserBlockService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.kbuddy_backend.common.exception.DuplicateException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.kbuddy_backend.common.exception.BadRequestException;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnnouncementService {

    private static final int MAX_ANNOUNCEMENT_IMAGES = 5;

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementImageRepository announcementImageRepository;
    private final S3Service s3Service;
    private final FCMService fcmService;


    @Transactional
    public AnnouncementResponse saveAnnouncement(AnnouncementSaveRequest announcementSaveRequest, List<MultipartFile> imageFiles, User user) {
        if(imageFiles != null && imageFiles.size() > MAX_ANNOUNCEMENT_IMAGES) {
            throw new BadRequestException("공지사항 게시글에는 이미지를 최대 " + MAX_ANNOUNCEMENT_IMAGES + "개까지 첨부할 수 있습니다.");
        }

        if(Objects.equals(announcementSaveRequest.title(), "")) {
            throw new BadRequestException("제목을 입력해주세요.");
        }
        if(Objects.equals(announcementSaveRequest.description(), "")) {
            throw new BadRequestException("내용을 입력해주세요.");
        }

        Announcement announcement = Announcement.builder()
                .writer(user)
                .title(announcementSaveRequest.title())
                .description(announcementSaveRequest.description())
                .build();

        if(imageFiles != null && !imageFiles.isEmpty()) {
            List<ImageFileDto> uploadedImages = uploadImages(imageFiles);
            saveImageFiles(uploadedImages, announcement);
        }

        Announcement saveAnnouncement = announcementRepository.save(announcement);
        return createAnnouncementResponseDto(saveAnnouncement, user);
    }

    private List<ImageFileDto> uploadImages(List<MultipartFile> imageFiles) {
        List<ImageFileDto> uploadedImages = new ArrayList<>();

        for (MultipartFile file : imageFiles) {
            // 이미지 파일 확인
            if(!s3Service.checkImageFile(file)) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
            }

            // S3에 파일 업로드
            S3Response s3Response = s3Service.saveFileWithUUID(file, "qna");

            // 파일 타입 결정(확장자 기반)
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

    public AllAnnouncementResponse getAllAnnouncement(int pageSize, Long announcementId, String title, SortBy sortBy, User currentUser) {
        List<Announcement> allAnnouncement = announcementRepository.paginationNoOffset(announcementId, title, pageSize, sortBy);

        List<AnnouncementPaginationResponse> announcementPaginationResponseList = allAnnouncement.stream()
                .map(announcement -> {
                    String thumbnailImageUrl = announcement.getImageUrls() != null && !announcement.getImageUrls().isEmpty()
                            ? announcement.getImageUrls().get(0).getImageUrl()
                            : "";
                    return AnnouncementPaginationResponse.of(
                            announcement.getId(),
                            announcement.getWriter().getUuid().toString(),
                            announcement.getWriter().getUsername(),
                            announcement.getWriter().getProfileImageUrl() != null ? announcement.getWriter().getProfileImageUrl() : "",
                            announcement.getTitle(),
                            announcement.getDescription(),
                            announcement.getViewCount(),
                            announcement.getCreatedDate(),
                            announcement.getLastModifiedDate(),
                            thumbnailImageUrl

                    );
                })
                .toList();

        Long nextId = getNextId(announcementPaginationResponseList, pageSize);

        return AllAnnouncementResponse.of(nextId, announcementPaginationResponseList);
    }

    private Long getNextId(List<AnnouncementPaginationResponse> responses, int pageSize) {
        if(responses.isEmpty() || responses.size() < pageSize) {
            return -1L;
        }

        return responses.stream()
                .map(AnnouncementPaginationResponse::id)
                .reduce((first, second) -> second) // 마지막 ID를 반환
                .orElse(-1L); // 리스트가 비어었으면 -1 반환
    }

    @Transactional
    public AnnouncementResponse getAnnouncement(Long announcementId, User currentUser) {
        Announcement announcementById = findAnnouncementById(announcementId);

        announcementById.plusViewCount();
        return createAnnouncementResponseDto(announcementById, currentUser);
    }

    @Transactional
    public AnnouncementResponse updateAnnouncement(Long announcementId, AnnouncementUpdateRequest announcementUpdateRequest, List<MultipartFile> newFiles, User user) {
        Announcement announcementById = findAnnouncementById(announcementId);
        isAnnouncementWriter(user, announcementById);

        int currentImageCount = announcementById.getImageUrls().size();
        int deletedImageCount = (announcementUpdateRequest.deleteImageIds() != null) ? announcementUpdateRequest.deleteImageIds().size() : 0;
        int newImageCount = (newFiles != null) ? newFiles.size() : 0;
        int finalImageCount = currentImageCount - deletedImageCount + newImageCount;

        if (finalImageCount > MAX_ANNOUNCEMENT_IMAGES) {
            throw new BadRequestException("공지사항 게시글에는 이미지를 최대 " + MAX_ANNOUNCEMENT_IMAGES + "개까지 첨부할 수 있습니다. (현재 " + finalImageCount + "개)");
        }

        if (announcementUpdateRequest.deleteImageIds() != null && !announcementUpdateRequest.deleteImageIds().isEmpty()) {
            deleteImages(announcementId, announcementUpdateRequest.deleteImageIds(), user);
        }

        if (newFiles != null && !newFiles.isEmpty()) {
            List<ImageFileDto> uploadedImages = uploadImages(newFiles);
            saveImageFiles(uploadedImages, announcementById);
        }

        announcementById.update(announcementUpdateRequest.title(), announcementUpdateRequest.description());
        return createAnnouncementResponseDto(announcementById, user);
    }

    private static void isAnnouncementWriter(User user, Announcement announcementById) {
        if (!Objects.equals(announcementById.getWriter().getUuid(), user.getUuid())) {
            throw new NotWriterException();
        }
    }

    @Transactional
    public void deleteImages(Long announcementId, List<Long> imageIds, User user) {
        Announcement announcement = findAnnouncementById(announcementId);
        isAnnouncementWriter(user, announcement);
        for (Long imageId : imageIds) {
            announcement.deleteImage(imageId);
            AnnouncementImage announcementImage = announcementImageRepository.findById(imageId).orElseThrow(AnnouncementImageNotFoundException::new);
            //s3에서도 이미지 삭제
            s3Service.deleteFile(announcementImage.getImageUrl());
        }
    }

    @Transactional
    public void deleteAnnouncement(Long announcementId, User user) {
        Announcement announcement = findAnnouncementById(announcementId);
        // 해당 게시글 작성자가 아닐 경우
        isAnnouncementWriter(user, announcement);

        // 게시글에 첨부된 이미지들도 S3에서 삭제
        List<AnnouncementImage> images = announcement.getImageUrls();
        for(AnnouncementImage image : images) {
            s3Service.deleteFile(image.getFilePath());
        }
        announcementRepository.delete(announcement);
    }

    private AnnouncementResponse createAnnouncementResponseDto(Announcement announcement, User currentUser){
        List<ImageFileDto> images = announcement.getImageUrls()
                .stream()
                .map(announcementImage -> ImageFileDto.of(announcementImage.getId(), announcementImage.getFileType(), announcementImage.getFilePath(),
                        announcementImage.getImageUrl()))
                .toList();

        return AnnouncementResponse.of(
                announcement.getId(),
                announcement.getWriter().getUuid().toString(),
                announcement.getWriter().getUsername(),
                announcement.getWriter().getProfileImageUrl() != null ? announcement.getWriter().getProfileImageUrl() : "",
                announcement.getTitle(),
                announcement.getDescription(),
                announcement.getViewCount(),
                announcement.getCreatedDate(),
                announcement.getLastModifiedDate(),
                images
        );
    }

    private void saveImageFiles(List<ImageFileDto> imageFiles, Announcement announcement) {
        for (ImageFileDto imageFile : imageFiles) {
            AnnouncementImage announcementImage = AnnouncementImage.builder()
                    .announcement(announcement)
                    .imageUrl(imageFile.url())
                    .filePath(imageFile.name())
                    .fileType(imageFile.type())
                    .build();
            announcement.addImage(announcementImage);
        }
    }

    public Announcement findAnnouncementById(Long announcementId) {
        return announcementRepository.findById(announcementId).orElseThrow(AnnouncementNotFoundException::new);
    }





















}
