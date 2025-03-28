package com.example.kbuddy_backend.blog.service;

import com.example.kbuddy_backend.blog.constant.BlogCategoryEnum;
import com.example.kbuddy_backend.blog.dto.request.BlogBookmarkRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogReportRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogSaveRequest;
import com.example.kbuddy_backend.blog.dto.request.BlogUpdateRequest;
import com.example.kbuddy_backend.blog.dto.response.AllBlogResponse;
import com.example.kbuddy_backend.blog.dto.response.BlogResponse;
import com.example.kbuddy_backend.blog.dto.response.BlogCommentResponse;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogCategory;
import com.example.kbuddy_backend.blog.entity.BlogCollection;
import com.example.kbuddy_backend.blog.entity.BlogHeart;
import com.example.kbuddy_backend.blog.entity.BlogBookmark;
import com.example.kbuddy_backend.blog.entity.BlogImage;
import com.example.kbuddy_backend.blog.entity.BlogReport;
import com.example.kbuddy_backend.blog.exception.BlogNotFoundException;
import com.example.kbuddy_backend.blog.exception.DuplicatedBlogHeartException;
import com.example.kbuddy_backend.blog.exception.NotWriterException;
import com.example.kbuddy_backend.blog.repository.BlogCategoryRepository;
import com.example.kbuddy_backend.blog.repository.BlogCollectionRepository;
import com.example.kbuddy_backend.blog.repository.BlogHeartRepository;
import com.example.kbuddy_backend.blog.repository.BlogReportRepository;
import com.example.kbuddy_backend.blog.repository.BlogRepository;
import com.example.kbuddy_backend.blog.repository.BlogBookmarkRepository;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.common.exception.BadRequestException;
import com.example.kbuddy_backend.common.exception.DuplicateException;
import com.example.kbuddy_backend.user.entity.User;
import java.util.Comparator;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.kbuddy_backend.blog.constant.SortBy;
import com.example.kbuddy_backend.blog.dto.response.BlogPaginationResponse;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogHeartRepository blogHeartRepository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final BlogCollectionRepository blogCollectionRepository;
    private final BlogBookmarkRepository blogBookmarkRepository;

    private final BlogReportRepository blogReportRepository;

    // 새로운 블로그를 저장
    @Transactional
    public BlogResponse saveBlog(BlogSaveRequest blogSaveRequest, User user) {

        List<ImageFileDto> imageFiles = blogSaveRequest.file();

        String hashtag = String.join(",", blogSaveRequest.hashtags());
        BlogCategory blogCategory = findCategoryById(blogSaveRequest.categoryId());

        Blog blog = Blog.builder()
                .title(blogSaveRequest.title())
                .description(blogSaveRequest.description())
                .category(blogCategory)
                .hashtag(hashtag)
                .writer(user)
                .build();

        if(imageFiles != null) {
            saveImageFiles(imageFiles, blog);
        }

        Blog saveBlog = blogRepository.save(blog);
        return createBlogResponseDto(saveBlog);
    }

//    public AllBlogResponse getAllBlog(int pageSize, Long blogId, String title, SortBy sortBy) {
//        List<Blog> allBlog = blogRepository.paginationNoOffset(blogId, title, pageSize, sortBy);
//        List<BlogPaginationResponse> blogPaginationResponseList = allBlog.stream()
//                .map(blog -> BlogPaginationResponse.of(blog.getId(), blog.getWriter().getId(), blog.getCategory().getId(),
//                        blog.getTitle(), blog.getDescription(), blog.getViewCount(), blog.getHeartCount(),
//                        blog.getCommentCount(), blog.getCreatedDate(),
//                        blog.getLastModifiedDate()))
//                .toList();
//
//        Long nextId = getNextId(blogPaginationResponseList);
//
//        return AllBlogResponse.of(nextId, blogPaginationResponseList);
//    }

    public AllBlogResponse getAllBlog(int pageSize, Long blogId, String title, SortBy sortBy, BlogCategoryEnum category) {
        if (sortBy == null) {
            sortBy = (category != null) ? SortBy.CATEGORY_LATEST : SortBy.LATEST;
        }

        List<Blog> allBlog = blogRepository.paginationNoOffset(blogId, title, pageSize, sortBy, category);
        List<BlogPaginationResponse> blogPaginationResponseList = allBlog.stream()
                .map(blog -> BlogPaginationResponse.of(blog.getId(), blog.getWriter().getId(), blog.getCategory().getId(),
                        blog.getTitle(), blog.getDescription(), blog.getViewCount(), blog.getHeartCount(),
                        blog.getCommentCount(), blog.getCreatedDate(),
                        blog.getLastModifiedDate()))
                .toList();

        Long nextId = getNextId(blogPaginationResponseList);

        return AllBlogResponse.of(nextId, blogPaginationResponseList);
    }

    private Long getNextId(List<BlogPaginationResponse> responses) {
        return responses.stream()
                .map(BlogPaginationResponse::id)
                .reduce((first, second) -> second) // 마지막 ID를 반환
                .orElse(-1L); // 리스트가 비어있으면 -1 반환
    }

    // 특정 블로그를 조회하고 조회수를 증가
    @Transactional
    public BlogResponse getBlog(Long blogId) {
        Blog blogById = findBlogById(blogId);
        blogById.plusViewCount();
        return createBlogResponseDto(blogById);
    }

    // 블로그 내용을 수정합니다.
    @Transactional
    public BlogResponse updateBlog(Long blogId, BlogUpdateRequest blogUpdateRequest, User user) {

        Blog blogById = findBlogById(blogId);

        isBlogWriter(user, blogById);
        
        String hashtag = String.join(",", blogUpdateRequest.hashtags());
        BlogCategory categoryById = findCategoryById(blogUpdateRequest.categoryId());
        blogById.update(blogUpdateRequest.title(), blogUpdateRequest.description(), hashtag, categoryById);
        return createBlogResponseDto(blogById);
    }

    private static void isBlogWriter(User user, Blog blogById) {
        if (!Objects.equals(blogById.getWriter().getId(), user.getId())) {
            throw new NotWriterException();
        }
    }

    @Transactional
    public void addImages(Long blogId, List<ImageFileDto> images, User user) {
        Blog blog = findBlogById(blogId);
        isBlogWriter(user, blog);
        saveImageFiles(images, blog);
    }

    @Transactional
    public void deleteImages(Long blogId, List<ImageFileDto> images, User user) {
        Blog blog = findBlogById(blogId);
        isBlogWriter(user, blog);
        for(ImageFileDto image : images) {
            blog.deleteImage(image.name());
        }
    }

    // 블로그를 삭제합니다.
    @Transactional
    public void deleteBlog(Long blogId, User user) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(BlogNotFoundException::new);

        // 해당 게시글 작성자가 아닌 경우
        isBlogWriter(user, blog);
        blogRepository.delete(blog);
    }

    // 블로그 엔티티를 응답 DTO로 변환합니다.
    private BlogResponse createBlogResponseDto(Blog blog) {
        List<ImageFileDto> images = blog.getImageUrls()
                .stream()
                .map(blogImage -> new ImageFileDto(blogImage.getFileType(), blogImage.getFilePath(),
                        blogImage.getImageUrl()
                ))
                .toList();
        List<BlogCommentResponse> comments = blog.getComments()
                .stream()
                .map(blogComment ->
                        BlogCommentResponse.of(blogComment.getId(), blogComment.getBlog().getId(),
                                blogComment.getWriter().getId(),
                                blogComment.getContent(), blogComment.getCreatedDate(),
                                blogComment.getLastModifiedDate()))
                .sorted(Comparator.comparing(BlogCommentResponse::createdAt)) // 만들어진 시간으로 오름차순 반환
                .toList();

        return BlogResponse.of(blog.getId(), blog.getWriter().getId(), blog.getCategory().getId(), blog.getTitle(),
                blog.getDescription(), blog.getViewCount(), blog.getCreatedDate(), blog.getLastModifiedDate(),
                images, comments, blog.getHeartCount(), blog.getCommentCount());
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
    public void addBookmark(BlogBookmarkRequest bookmarkRequest, Long blogId) {
        BlogCollection collectionById = findCollectionById(bookmarkRequest.collectionId());
        BlogBookmark blogBookmark = new BlogBookmark(findBlogById(blogId), collectionById);
        collectionById.addBookmark(blogBookmark);
    }

    //블로그를 북마크에서 제거합니다.
    @Transactional
    public void removeBookmark(BlogBookmarkRequest bookmarkRequest, Long blogId) {
        BlogCollection collectionById = findCollectionById(bookmarkRequest.collectionId());
        BlogBookmark blogBookmark = blogBookmarkRepository.findByBlog(findBlogById(blogId))
                .orElseThrow(() -> new IllegalArgumentException("북마크 된 Blog가 아닙니다."));
        collectionById.removeBookmark(blogBookmark);
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

    public Blog findBlogById(Long blogId) {
        return blogRepository.findById(blogId).orElseThrow(BlogNotFoundException::new);
    }

    private BlogCategory findCategoryById(Long categoryId) {
        return blogCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
    }

    public BlogCollection findCollectionById(Long bookmarkId) {
        return blogCollectionRepository.findById(bookmarkId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 컬렉션입니다."));
    }
}