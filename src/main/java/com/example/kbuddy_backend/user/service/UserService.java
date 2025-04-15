package com.example.kbuddy_backend.user.service;

import com.example.kbuddy_backend.blog.constant.BlogStatus;
import com.example.kbuddy_backend.blog.entity.Blog;
import com.example.kbuddy_backend.blog.entity.BlogImage;
import com.example.kbuddy_backend.blog.repository.BlogRepository;
import com.example.kbuddy_backend.common.dto.ImageFileDto;
import com.example.kbuddy_backend.qna.constant.QnaStatus;
import com.example.kbuddy_backend.qna.dto.response.QnaPaginationResponse;
import com.example.kbuddy_backend.qna.entity.Qna;
import com.example.kbuddy_backend.qna.repository.QnaRepository;
import com.example.kbuddy_backend.user.dto.request.UserBioRequest;
import com.example.kbuddy_backend.user.dto.response.DraftListResponse;
import com.example.kbuddy_backend.user.dto.response.UserAuthorityResponse;
import com.example.kbuddy_backend.user.dto.response.UserResponse;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;

import java.util.ArrayList;
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
    private final String QnaType = "Q&A";
    private final String BlogType = "Blog";

    public UserResponse getUser(User user) {
        User findUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));
        List<String> authorities = findUser.getAuthorities().stream()
                .map(authority -> authority.getAuthorityName().name())
                .toList();
        return UserResponse.of(findUser.getId(), findUser.getUsername(), findUser.getEmail(), authorities,
                findUser.getProfileImageUrl(), findUser.getBio(), findUser.getFirstName(), findUser.getLastName(),
                findUser.getCreatedDate(), findUser.getGender(), findUser.getCountry(), findUser.isActive());
    }

    @Transactional
    public void saveUserBio(UserBioRequest request, User user) {
        user.updateBio(request.bio());
    }

    public List<DraftListResponse> getMyDrafts(User user) {
        // Qna draft 조회
        List<Qna> draftQnas = qnaRepository.findByWriterAndStatus(user, QnaStatus.DRAFT);
        List<DraftListResponse> qnaDrafts = draftQnas.stream()
                .map(qna -> DraftListResponse.of(
                        qna.getId(),
                        qna.getWriter().getId(),
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
                        blog.getWriter().getId(),
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

        return allDrafts;
    }
}
