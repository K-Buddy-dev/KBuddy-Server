package com.example.kbuddy_backend.user.service;

import com.example.kbuddy_backend.blog.constant.BlogStatus;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogBookmark;
import com.example.kbuddy_backend.blog.repository.BlogBookmarkRepository;
import com.example.kbuddy_backend.blog.repository.BlogHeartRepository;
import com.example.kbuddy_backend.blog.repository.BlogRepository;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.qna.constant.QnaStatus;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.entity.QnaBookmark;
import com.example.kbuddy_backend.qna.repository.QnaBookmarkRepository;
import com.example.kbuddy_backend.qna.repository.QnaHeartRepository;
import com.example.kbuddy_backend.qna.repository.QnaRepository;
import com.example.kbuddy_backend.user.dto.response.BookmarkedPostResponse;
import com.example.kbuddy_backend.user.dto.response.DraftListResponse;
import com.example.kbuddy_backend.user.dto.response.UserProfileResponse;
import com.example.kbuddy_backend.user.dto.response.UserResponse;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;
import com.example.kbuddy_backend.s3.service.S3Service;
import com.example.kbuddy_backend.user.exception.UserNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import com.example.kbuddy_backend.s3.dto.response.S3Response;
import com.example.kbuddy_backend.common.exception.BadRequestException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final QnaRepository qnaRepository;
    private final BlogRepository blogRepository;
    private final QnaBookmarkRepository qnaBookmarkRepository;
    private final BlogBookmarkRepository blogBookmarkRepository;
    private final QnaHeartRepository qnaHeartRepository;
    private final BlogHeartRepository blogHeartRepository;
    private final S3Service s3Service;
    private final String QnaType = "Q&A";
    private final String BlogType = "Blog";

    public UserResponse getUser(User user) {
        User findUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));
        List<String> authorities = findUser.getAuthorities().stream()
                .map(authority -> authority.getAuthorityName().name())
                .toList();
        return UserResponse.of(findUser.getUuid().toString(), findUser.getUsername(), findUser.getEmail(), authorities,
                findUser.getProfileImageUrl(), findUser.getBio(), findUser.getFirstName(), findUser.getLastName(),
                findUser.getCreatedDate(), findUser.getGender(), findUser.getCountry(), findUser.isActive());
    }

    @Transactional
    public UserProfileResponse updateUserProfile(User currentUser, String bio, MultipartFile profileImageFile) {
        User managedUser = userRepository.findById(currentUser.getId())
                .orElseThrow(UserNotFoundException::new);

        String oldImageUrl = managedUser.getProfileImageUrl();
        String newProfileImageUrl = oldImageUrl;
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            if (!s3Service.checkImageFile(profileImageFile)) {
                throw new BadRequestException("이미지 파일만 업로드 가능합니다. (jpg, png, jpeg)");
            }
            S3Response s3Response = s3Service.saveFileWithUUID(profileImageFile, "profile");
            newProfileImageUrl = s3Response.s3ImageUrl();

            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                s3Service.deleteFile(oldImageUrl);
            }
        }
        managedUser.updateProfile(bio, newProfileImageUrl);
        return UserProfileResponse.of(managedUser.getBio(), managedUser.getUsername(), managedUser.getProfileImageUrl());
    }

    public List<DraftListResponse> getMyDrafts(User user) {
        // Qna draft 조회
        List<Qna> draftQnas = qnaRepository.findByWriterAndStatus(user, QnaStatus.DRAFT);
        List<DraftListResponse> qnaDrafts = draftQnas.stream()
                .map(qna -> DraftListResponse.of(
                        qna.getId(),
                        qna.getWriter().getUuid().toString(),
                        QnaType,
                        qna.getCategoryCode(),
                        qna.getTitle(),
                        qna.getDescription(),
                        qna.getImageUrls().stream().map(
                                (image) -> ImageFileDto.of(
                                        image.getId(),
                                        image.getFileType(),
                                        image.getFilePath(),
                                        image.getImageUrl()
                                )
                        ).collect(Collectors.toList()),
                        qna.getCreatedDate(),
                        qna.getLastModifiedDate(),
                        qna.getStatus().toString()
                ))
                .toList();

        // Blog draft 조회
        List<Blog> draftBlogs = blogRepository.findByWriterAndStatus(user, BlogStatus.DRAFT);
        List<DraftListResponse> blogDrafts = draftBlogs.stream()
                .map(blog -> DraftListResponse.of(
                        blog.getId(),
                        blog.getWriter().getUuid().toString(),
                        BlogType,
                        blog.getCategoryCode(),
                        blog.getTitle(),
                        blog.getDescription(),
                        blog.getImageUrls().stream().map(
                                (image) -> ImageFileDto.of(
                                        image.getId(),
                                        image.getFileType(),
                                        image.getFilePath(),
                                        image.getImageUrl()
                                )
                        ).collect(Collectors.toList()),
                        blog.getCreatedDate(),
                        blog.getLastModifiedDate(),
                        blog.getStatus().toString()))
                .toList();

        List<DraftListResponse> allDrafts = new ArrayList<>();
        allDrafts.addAll(qnaDrafts);
        allDrafts.addAll(blogDrafts);
        allDrafts.sort(Comparator.comparing(DraftListResponse::createdAt).reversed());
        return allDrafts;
    }

    public List<BookmarkedPostResponse> getMyBookmarks(User user) {
        // QNA 북마크 조회
        List<QnaBookmark> qnaBookmarks = qnaBookmarkRepository.findByUserId(user.getId());
        List<BookmarkedPostResponse> qnaBookmarkedPosts = qnaBookmarks.stream()
                .map(qnaBookmark -> {
                    Qna qna = qnaBookmark.getQna();
                    String thumbnailImageUrl = qna.getImageUrls() != null && !qna.getImageUrls().isEmpty()
                            ? qna.getImageUrls().get(0).getImageUrl()
                            : "";
                    return BookmarkedPostResponse.of(
                            qna.getId(),
                            qna.getWriter().getUuid().toString(),
                            qna.getWriter().getUsername(),
                            qna.getWriter().getProfileImageUrl() != null ? qna.getWriter().getProfileImageUrl() : "",
                            "QNA",
                            List.of(qna.getCategoryCode()),
                            qna.getTitle(),
                            qna.getDescription(),
                            qna.getViewCount(),
                            qna.getHeartCount(),
                            qna.getCommentCount(),
                            qna.getCreatedDate(),
                            qna.getLastModifiedDate(),
                            thumbnailImageUrl,
                            qnaHeartRepository.existsByQnaIdAndUserId(qna.getId(), user.getId()),
                            true // 북마크된 게시글이므로 true
                    );
                })
                .toList();

        // 블로그 북마크 조회
        List<BlogBookmark> blogBookmarks = blogBookmarkRepository.findByUserId(user.getId());
        List<BookmarkedPostResponse> blogBookmarkedPosts = blogBookmarks.stream()
                .map(blogBookmark -> {
                    Blog blog = blogBookmark.getBlog();
                    String thumbnailImageUrl = blog.getImageUrls() != null && !blog.getImageUrls().isEmpty()
                            ? blog.getImageUrls().get(0).getImageUrl()
                            : "";
                    return BookmarkedPostResponse.of(
                            blog.getId(),
                            blog.getWriter().getUuid().toString(),
                            blog.getWriter().getUsername(),
                            blog.getWriter().getProfileImageUrl() != null ? blog.getWriter().getProfileImageUrl() : "",
                            "BLOG",
                            blog.getCategoryCode(),
                            blog.getTitle(),
                            blog.getDescription(),
                            blog.getViewCount(),
                            blog.getHeartCount(),
                            blog.getCommentCount(),
                            blog.getCreatedDate(),
                            blog.getLastModifiedDate(),
                            thumbnailImageUrl,
                            blogHeartRepository.existsByBlogIdAndUserId(blog.getId(), user.getId()),
                            true // 북마크된 게시글이므로 true
                    );
                })
                .toList();

        // 모든 북마크를 합치고 생성일 기준으로 정렬
        List<BookmarkedPostResponse> allBookmarks = new ArrayList<>();
        allBookmarks.addAll(qnaBookmarkedPosts);
        allBookmarks.addAll(blogBookmarkedPosts);
        allBookmarks.sort(Comparator.comparing(BookmarkedPostResponse::createdAt).reversed());
        
        return allBookmarks;
    }
}
