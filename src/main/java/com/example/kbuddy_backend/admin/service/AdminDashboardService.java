package com.example.kbuddy_backend.admin.service;

import com.example.kbuddy_backend.admin.dto.AdminPostType;
import com.example.kbuddy_backend.admin.dto.response.AdminReportedPostResponse;
import com.example.kbuddy_backend.admin.dto.response.AdminReportedPostsResponse;
import com.example.kbuddy_backend.admin.dto.response.AdminReportedUserResponse;
import com.example.kbuddy_backend.admin.dto.response.AdminReportedUsersResponse;
import com.example.kbuddy_backend.admin.dto.response.AdminSubscriberDetailResponse;
import com.example.kbuddy_backend.admin.dto.response.AdminSubscriberListResponse;
import com.example.kbuddy_backend.admin.dto.response.AdminSubscriberStatsResponse;
import com.example.kbuddy_backend.blog.repository.BlogReportRepository;
import com.example.kbuddy_backend.blog.repository.BlogReportSummary;
import com.example.kbuddy_backend.qna.repository.QnaReportRepository;
import com.example.kbuddy_backend.qna.repository.QnaReportSummary;
import com.example.kbuddy_backend.user.constant.Gender;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final BlogReportRepository blogReportRepository;
    private final QnaReportRepository qnaReportRepository;

    public AdminSubscriberStatsResponse getSubscriberStats() {
        long total = userRepository.count();
        long male = userRepository.countByGender(Gender.M);
        long female = userRepository.countByGender(Gender.F);
        return AdminSubscriberStatsResponse.builder()
                .totalSubscribers(total)
                .maleSubscribers(male)
                .femaleSubscribers(female)
                .build();
    }

    public AdminSubscriberListResponse getSubscribers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        List<AdminSubscriberDetailResponse> subscribers = page.getContent().stream()
                .map(user -> AdminSubscriberDetailResponse.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .gender(user.getGender())
                        .active(user.isActive())
                        .createdAt(user.getCreatedDate())
                        .build())
                .toList();

        return AdminSubscriberListResponse.builder()
                .totalElements(page.getTotalElements())
                .page(page.getNumber())
                .size(page.getSize())
                .subscribers(subscribers)
                .build();
    }

    public AdminReportedPostsResponse getReportedPosts() {
        List<AdminReportedPostResponse> posts = collectReportedPosts();
        posts.sort(Comparator.comparingLong(AdminReportedPostResponse::getReportCount).reversed());
        return AdminReportedPostsResponse.builder()
                .totalPosts(posts.size())
                .posts(posts)
                .build();
    }

    public AdminReportedUsersResponse getReportedUsers() {
        List<AdminReportedPostResponse> posts = collectReportedPosts();
        Map<Long, List<AdminReportedPostResponse>> groupedByUser = posts.stream()
                .collect(Collectors.groupingBy(AdminReportedPostResponse::getWriterId));

        List<AdminReportedUserResponse> users = groupedByUser.values().stream()
                .map(userPosts -> {
                    AdminReportedPostResponse representative = userPosts.get(0);
                    long totalReports = userPosts.stream()
                            .mapToLong(AdminReportedPostResponse::getReportCount)
                            .sum();
                    return AdminReportedUserResponse.builder()
                            .userId(representative.getWriterId())
                            .username(representative.getWriterUsername())
                            .email(representative.getWriterEmail())
                            .totalReports(totalReports)
                            .posts(userPosts.stream()
                                    .sorted(Comparator.comparingLong(AdminReportedPostResponse::getReportCount).reversed())
                                    .toList())
                            .build();
                })
                .sorted(Comparator.comparingLong(AdminReportedUserResponse::getTotalReports).reversed())
                .toList();

        return AdminReportedUsersResponse.builder()
                .totalUsers(users.size())
                .users(users)
                .build();
    }

    private List<AdminReportedPostResponse> collectReportedPosts() {
        List<AdminReportedPostResponse> posts = new ArrayList<>();
        List<BlogReportSummary> blogSummaries = blogReportRepository.findReportSummaries();
        for (BlogReportSummary summary : blogSummaries) {
            posts.add(AdminReportedPostResponse.builder()
                    .postType(AdminPostType.BLOG)
                    .postId(summary.getBlogId())
                    .title(summary.getBlogTitle())
                    .writerId(summary.getWriterId())
                    .writerUsername(summary.getWriterUsername())
                    .writerEmail(summary.getWriterEmail())
                    .reportCount(summary.getReportCount())
                    .build());
        }

        List<QnaReportSummary> qnaSummaries = qnaReportRepository.findReportSummaries();
        for (QnaReportSummary summary : qnaSummaries) {
            posts.add(AdminReportedPostResponse.builder()
                    .postType(AdminPostType.QNA)
                    .postId(summary.getQnaId())
                    .title(summary.getQnaTitle())
                    .writerId(summary.getWriterId())
                    .writerUsername(summary.getWriterUsername())
                    .writerEmail(summary.getWriterEmail())
                    .reportCount(summary.getReportCount())
                    .build());
        }
        return posts;
    }
}

