package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.constant.BlogCategories;
import com.example.kbuddy_backend.blog.constant.BlogStatus;
import com.example.kbuddy_backend.blog.constant.BlogType;
import com.example.kbuddy_backend.blog.dto.request.BlogReportRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogSaveRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogUpdateRequest;
import com.example.kbuddy_backend.blog.dto.response.AllBlogResponse;
import com.example.kbuddy_backend.blog.dto.response.BlogResponse;
import com.example.kbuddy_backend.blog.dto.response.BlogCommentResponse;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogHeart;
import com.example.kbuddy_backend.blog.entity.BlogBookmark;
import com.example.kbuddy_backend.blog.entity.BlogImage;
import com.example.kbuddy_backend.blog.entity.BlogReport;
import com.example.kbuddy_backend.blog.entity.BlogComment;
import com.example.kbuddy_backend.blog.exception.*;
import com.example.kbuddy_backend.blog.repository.*;
import com.example.kbuddy_backend.common.constant.ImageFileType;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.common.exception.BadRequestException;
import com.example.kbuddy_backend.common.exception.DuplicateException;
import com.example.kbuddy_backend.notification.entity.NotificationType;
import com.example.kbuddy_backend.notification.service.NotificationService;
import com.example.kbuddy_backend.qna.constant.QnaStatus;
import com.example.kbuddy_backend.s3.dto.response.S3Response;
import com.example.kbuddy_backend.s3.service.S3Service;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.service.UserBlockService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.kbuddy_backend.blog.constant.SortBy;
import com.example.kbuddy_backend.blog.dto.response.BlogPaginationResponse;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlogService {

    private static final int MAX_BLOG_IMAGES = 10; // Define max image count constant for blog

    private final BlogRepository blogRepository;
    private final BlogHeartRepository blogHeartRepository;
    private final BlogBookmarkRepository blogBookmarkRepository;
    private final BlogImageRepository blogImageRepository;
    private final BlogReportRepository blogReportRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final S3Service s3Service;
    private final UserBlockService userBlockService;
    private final NotificationService notificationService;

    // 새로운 블로그를 저장
    @Transactional
    public BlogResponse saveBlog(BlogSaveRequest blogSaveRequest, List<MultipartFile> imageFiles, User user) {
        // Validate image count before proceeding
        if (imageFiles != null && imageFiles.size() > MAX_BLOG_IMAGES) {
            throw new BadRequestException("블로그 게시글에는 이미지를 최대 " + MAX_BLOG_IMAGES + "개까지 첨부할 수 있습니다.");
        }

        if(blogSaveRequest.status() == BlogStatus.PUBLISHED) {
            if (Objects.equals(blogSaveRequest.title(), "")) {
                throw new BadRequestException("제목을 입력해주세요.");
            }
            if (Objects.equals(blogSaveRequest.description(), "")) {
                throw new BadRequestException("내용을 입력해주세요.");
            }
        }

        BlogType blogType = blogSaveRequest.resolvedType();
        validateCategoryIds(blogSaveRequest.categoryId(), blogType);

        String hashTag = "";
        if (blogSaveRequest.hashtags() != null && !blogSaveRequest.hashtags().isEmpty()) {
            hashTag = String.join(",", blogSaveRequest.hashtags());
        }

        Blog blog = Blog.builder()
                .title(blogSaveRequest.title())
                .description(blogSaveRequest.description())
                .category(blogSaveRequest.categoryId())
                .hashtag(hashTag)
                .writer(user)
                .status(blogSaveRequest.status())
                .type(blogType)
                .build();

        // 이미지가 있으면 S3에 업로드하고 연결
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<ImageFileDto> uploadedImages = uploadImages(imageFiles);
            saveImageFiles(uploadedImages, blog);
        }

        Blog saveBlog = blogRepository.save(blog);
        return createBlogResponseDto(saveBlog,user);
    }

    // 이미지 파일들을 S3에 업로드하고 ImageFileDto 리스트를 반환합니다.
    private List<ImageFileDto> uploadImages(List<MultipartFile> imageFiles) {
        List<ImageFileDto> uploadedImages = new ArrayList<>();

        for (MultipartFile file : imageFiles) {
            // 이미지 파일 확인
            if(!s3Service.checkImageFile(file)) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
            }

            // S3에 파일 업로드
            S3Response s3Response = s3Service.saveFileWithUUID(file, "blog");

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

    public AllBlogResponse getAllBlog(int pageSize, Long blogId, String title, SortBy sortBy, Integer categoryCode, BlogType type, User currentUser) {
        List<Blog> allBlog = blogRepository.paginationNoOffset(blogId, title, pageSize, sortBy, categoryCode, BlogStatus.PUBLISHED, type);
        
        // 차단된 사용자 필터링
        if (currentUser != null) {
            List<Long> blockedUserIds = userBlockService.getBlockedUserIds(currentUser);
            allBlog = allBlog.stream()
                    .filter(blog -> !blockedUserIds.contains(blog.getWriter().getId()))
                    .toList();
        }
        
        List<BlogPaginationResponse> blogPaginationResponseList = allBlog.stream()
                .map(blog -> {
                    boolean isBookmarked = currentUser != null && blogBookmarkRepository.existsByBlogIdAndUserId(blog.getId(), currentUser.getId());
                    boolean isHearted = currentUser != null && blogHeartRepository.existsByBlogIdAndUserId(blog.getId(), currentUser.getId());
                    String thumbnailImageUrl = blog.getImageUrls() != null && !blog.getImageUrls().isEmpty()
                            ? blog.getImageUrls().get(0).getImageUrl()
                            : "";
                    return BlogPaginationResponse.of(
                            blog.getId(),
                            blog.getType(),
                            blog.getWriter().getUuid().toString(),
                            blog.getWriter().getUsername(),
                            blog.getWriter().getProfileImageUrl() != null ? blog.getWriter().getProfileImageUrl() : "",
                            blog.getCategoryCode(),
                            blog.getTitle(),
                            blog.getDescription(),
                            blog.getViewCount(),
                            blog.getHeartCount(),
                            blog.getCommentCount(),
                            blog.getCreatedDate(),
                            blog.getLastModifiedDate(),
                            blog.getStatus(),
                            isBookmarked,
                            isHearted,
                            thumbnailImageUrl
                    );
                })
                .toList();

        Long nextId = getNextId(blogPaginationResponseList, pageSize);

        return AllBlogResponse.of(nextId, blogPaginationResponseList);
    }

    private Long getNextId(List<BlogPaginationResponse> responses, int pageSize) {
        if (responses.isEmpty() || responses.size() < pageSize) {
            return -1L;
        }
        return responses.stream()
                .map(BlogPaginationResponse::id)
                .reduce((first, second) -> second) // 마지막 ID를 반환
                .orElse(-1L); //리스트가 비어있으면 -1 반환
    }

    // 특정 블로그를 조회하고 조회수를 증가
    @Transactional
    public BlogResponse getBlog(Long blogId, User currentUser) {
        Blog blogById = findBlogById(blogId);
        
        // 차단된 사용자의 게시글인지 확인
        if (currentUser != null && userBlockService.isBlocked(currentUser, blogById.getWriter())) {
            throw new AccessDeniedException("차단된 사용자의 게시글은 조회할 수 없습니다.");
        }
        
        if (blogById.getStatus() == BlogStatus.DRAFT) {
            if (currentUser == null) {
                throw new AccessDeniedException("로그인이 필요합니다.");
            }
            if (!Objects.equals(blogById.getWriter().getUuid(), currentUser.getUuid())) {
                throw new AccessDeniedException("임시 저장된 글은 작성자만 조회할 수 있습니다."); // Or use NotWriterException
            }
        }

        blogById.plusViewCount();
        return createBlogResponseDto(blogById,currentUser);
    }

    // 블로그 내용을 수정합니다.
    @Transactional
    public BlogResponse updateBlog(Long blogId, BlogUpdateRequest blogUpdateRequest, List<MultipartFile> newFiles, User user) {
        Blog blogById = findBlogById(blogId);
        isBlogWriter(user, blogById);

        // Calculate final image count for validation
        int currentImageCount = blogById.getImageUrls().size();
        // Assuming BlogUpdateRequest has deleteImageIds and newFiles (adjust if names differ)
        int deletedImageCount = (blogUpdateRequest.deleteImageIds() != null) ? blogUpdateRequest.deleteImageIds().size() : 0;
        int newImageCount = (newFiles != null) ? newFiles.size() : 0;
        int finalImageCount = currentImageCount - deletedImageCount + newImageCount;

        if (finalImageCount > MAX_BLOG_IMAGES) {
            throw new BadRequestException("블로그 게시글에는 이미지를 최대 " + MAX_BLOG_IMAGES + "개까지 첨부할 수 있습니다. (현재 " + finalImageCount + "개)");
        }

        // Proceed with updates only after validation passes
        String hashTag = "";
        if (blogUpdateRequest.hashtags() != null && !blogUpdateRequest.hashtags().isEmpty()) {
            hashTag = String.join(",", blogUpdateRequest.hashtags());
        }

        // Handle image deletions
        if (blogUpdateRequest.deleteImageIds() != null && !blogUpdateRequest.deleteImageIds().isEmpty()) {
            deleteImages(blogId, blogUpdateRequest.deleteImageIds(), user);
        }

        // Handle new image uploads
        if (newFiles != null && !newFiles.isEmpty()) {
            List<ImageFileDto> uploadedImages = uploadImages(newFiles);
            saveImageFiles(uploadedImages, blogById);
        }

        // categoryId 수정 시 타입에 맞는 범위 검증
        if (blogUpdateRequest.categoryId() != null) {
            validateCategoryIds(blogUpdateRequest.categoryId(), blogById.getType());
        }

        // Update Blog entity
        blogById.update(blogUpdateRequest.title(), blogUpdateRequest.description(), hashTag, blogUpdateRequest.categoryId(),
                blogUpdateRequest.status());

        return createBlogResponseDto(blogById, user);
    }

    private static void isBlogWriter(User user, Blog blogById) {
        if (!Objects.equals(blogById.getWriter().getUuid(), user.getUuid())) {
            throw new NotWriterException();
        }
    }

    @Transactional
    public void deleteImages(Long blogId, List<Long> imageIds, User user) {
        Blog blog = findBlogById(blogId);
        isBlogWriter(user, blog);

        for(Long imageId : imageIds) {
            blog.deleteImage(imageId);
            BlogImage blogImage = blogImageRepository.findById(imageId).orElseThrow(BlogImageNotFoundException::new);
            // S3에서도 이미지 삭제
            s3Service.deleteFile(blogImage.getImageUrl());
        }
    }

    // 블로그를 삭제합니다.
    @Transactional
    public void deleteBlog(Long blogId, User user) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(BlogNotFoundException::new);

        // 해당 게시글 작성자가 아닌 경우
        isBlogWriter(user, blog);

        // 게시글에 첨부된 이미지들도 S3에서 삭제
        List<BlogImage> images = blog.getImageUrls();
        for (BlogImage image : images) {
            s3Service.deleteFile(image.getFilePath());
        }

        blog.markDeleted();
    }

    // 블로그 엔티티를 응답 DTO로 변환합니다.
    private BlogResponse createBlogResponseDto(Blog blog, User currentUser) {
        List<ImageFileDto> images = blog.getImageUrls()
                .stream()
                .map(blogImage -> ImageFileDto.of(
                        blogImage.getId(),
                        blogImage.getFileType(),
                        blogImage.getFilePath(),
                        blogImage.getImageUrl()
                ))
                .toList();

        boolean isBookmarked = currentUser != null && blogBookmarkRepository.existsByBlogIdAndUserId(blog.getId(), currentUser.getId());
        boolean isHearted = currentUser != null && blogHeartRepository.existsByBlogIdAndUserId(blog.getId(), currentUser.getId());

        List<BlogComment> comments = blogCommentRepository.findCommentsWithWriterAndChildren(blog.getId());
        
        // 모든 댓글과 답글의 ID 수집
        List<Long> allCommentIds = comments.stream()
                .flatMap(comment -> {
                    List<Long> ids = new ArrayList<>();
                    ids.add(comment.getId());
                    ids.addAll(comment.getChildren().stream()
                            .map(BlogComment::getId)
                            .toList());
                    return ids.stream();
                })
                .toList();

        // 좋아요 수 조회
        Map<Long, Long> heartCounts = blogCommentRepository.findCommentHeartCounts(allCommentIds);
        
        // 사용자가 좋아요한 댓글 ID 조회
        Set<Long> heartedCommentIds = currentUser == null
                ? Set.of()
                : blogCommentRepository.findHeartedCommentIds(allCommentIds, currentUser.getId());

        List<BlogCommentResponse> commentResponses = comments.stream()
                .filter(comment -> currentUser == null || !userBlockService.isBlocked(currentUser, comment.getWriter()))
                .map(comment -> {
                    List<BlogCommentResponse> replies = comment.getChildren()
                            .stream()
                            .filter(reply -> currentUser == null || !userBlockService.isBlocked(currentUser, reply.getWriter()))
                            .map(reply -> BlogCommentResponse.of(
                                    reply.getId(),
                                    reply.getBlog().getId(),
                                    reply.getWriter().getUuid().toString(),
                                    reply.getWriter().getUsername(),
                                    reply.getWriter().getProfileImageUrl() != null ? reply.getWriter().getProfileImageUrl() : "",
                                    reply.getContent(),
                                    reply.getCreatedDate(),
                                    reply.getLastModifiedDate(),
                                    List.of(),
                                    heartCounts.getOrDefault(reply.getId(), 0L).intValue(),
                                    heartedCommentIds.contains(reply.getId())
                            ))
                            .sorted(Comparator.comparing(BlogCommentResponse::createdAt))
                            .toList();

                    return BlogCommentResponse.of(
                            comment.getId(),
                            comment.getBlog().getId(),
                            comment.getWriter().getUuid().toString(),
                            comment.getWriter().getUsername(),
                            comment.getWriter().getProfileImageUrl() != null ? comment.getWriter().getProfileImageUrl() : "",
                            comment.getContent(),
                            comment.getCreatedDate(),
                            comment.getLastModifiedDate(),
                            replies,
                            heartCounts.getOrDefault(comment.getId(), 0L).intValue(),
                            heartedCommentIds.contains(comment.getId())
                    );
                })
                .sorted(Comparator.comparing(BlogCommentResponse::createdAt))
                .toList();

        return BlogResponse.of(
                blog.getId(),
                blog.getType(),
                blog.getWriter().getUuid().toString(),
                blog.getWriter().getUsername(),
                blog.getWriter().getProfileImageUrl() != null ? blog.getWriter().getProfileImageUrl() : "",
                blog.getCategoryCode(),
                blog.getTitle(),
                blog.getDescription(),
                blog.getViewCount(),
                blog.getCreatedDate(),
                blog.getLastModifiedDate(),
                images,
                commentResponses,
                blog.getHeartCount(),
                blog.getCommentCount(),
                isBookmarked,
                isHearted,
                blog.getStatus()
        );
    }

    private void saveImageFiles(List<ImageFileDto> imageFiles, Blog blog) {
        for (ImageFileDto imageFile : imageFiles) {
            BlogImage blogImage = BlogImage.builder()
                    .blog(blog)
                    .imageUrl(imageFile.url())
                    .filePath(imageFile.name())
                    .fileType(imageFile.type())
                    .build();
            blog.addImage(blogImage);
        }
    }

    // 블로그에 좋아요를 추가합니다.
    @Transactional
    public void plusHeart(Long blogId, User user) {
        blogHeartRepository.findByBlogIdAndUserId(blogId, user.getId())
                .ifPresent(blogheart -> {
                    throw new DuplicatedBlogHeartException(); // 좋아요를 이미 누른 경우
                });
        Blog blog = findBlogById(blogId);
        BlogHeart blogHeart = new BlogHeart(user, blog);
        blog.plusHeart(blogHeart);
        blogHeartRepository.save(blogHeart);

        //알림 전송
        if (!Objects.equals(blog.getWriter().getId(), user.getId())) {
            notificationService.notify(blog.getWriter(), "Your Blog Post Got a New Like",
                    user.getUsername() + " liked your blog post.",
                    NotificationType.BLOG_LIKE_NOTIFICATION, blogId.toString());
        }

    }

    //블로그의 좋아요를 취소
    @Transactional
    public void minusHeart(Long blogId, User user) {

        Blog blogById = findBlogById(blogId);
        BlogHeart byBlogIdAndUserId = blogHeartRepository.findByBlogIdAndUserId(blogId, user.getId())
                .orElseThrow(BlogNotFoundException::new);
        blogById.minusHeart(byBlogIdAndUserId);
        blogHeartRepository.deleteByBlogIdAndUserId(blogId, user.getId());
    }

    //블로그를 북마크에 추가합니다.
    @Transactional
    public void addBookmark(User user, Long blogId) {
        BlogBookmark blogBookmark = new BlogBookmark(findBlogById(blogId), user);
        blogBookmarkRepository.save(blogBookmark);
    }

    //블로그를 북마크에서 제거합니다.
    @Transactional
    public void removeBookmark(User user, Long blogId) {
        BlogBookmark blogBookmark = blogBookmarkRepository.findByBlogIdAndUserId(blogId, user.getId())
                .orElseThrow(BlogBookmarkNotFoundException::new);
        blogBookmarkRepository.delete(blogBookmark);
    }

    // 블로그를 신고합니다.
    @Transactional
    public void reportBlog(Long blogId, BlogReportRequest request, User user) {
        // 신고하려는 블로그가 존재하는지 확인
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(BlogNotFoundException::new);

        // 이미 신고한 블로그인지 확인
        if (blogReportRepository.existsByBlogIdAndReporterId(blogId, user.getId())) {
            throw new DuplicateException("이미 신고한 블로그 입니다.");
        }

        // 자신이 쓴 블로그 신고하는지 확인
        if (blog.getWriter().getId().equals(user.getId())) {
            throw new BadRequestException(("자신의 블로그 글을 신고할 수 없습니다."));
        }

        BlogReport report = BlogReport.builder()
                .blog(blog)
                .reporter(user)
                .content(request.content())
                .build();

        blogReportRepository.save(report);
        blog.plusReportCount();
    }

    private void validateCategoryIds(List<Integer> categoryIds, BlogType type) {
        if (categoryIds == null) return;
        for (Integer categoryId : categoryIds) {
            if (!BlogCategories.isValidCategoryCode(categoryId, type)) {
                throw new BadRequestException(
                        type + " 타입에서 허용되지 않는 categoryId입니다: " + categoryId
                );
            }
        }
    }

    public Blog findBlogById(Long blogId) {
        return blogRepository.findById(blogId).orElseThrow(BlogNotFoundException::new);
    }

    public Page<BlogResponse> findBlogs(Pageable pageable, User currentUser) {
        return blogRepository.findAll(pageable)
                .map(blog -> createBlogResponseDto(blog, currentUser));
    }
}