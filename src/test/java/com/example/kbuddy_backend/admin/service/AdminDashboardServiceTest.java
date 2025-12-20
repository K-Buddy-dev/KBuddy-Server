package com.example.kbuddy_backend.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.example.kbuddy_backend.admin.dto.AdminPostType;
import com.example.kbuddy_backend.admin.dto.response.AdminReportedPostsResponse;
import com.example.kbuddy_backend.admin.dto.response.AdminReportedUsersResponse;
import com.example.kbuddy_backend.admin.dto.response.AdminSubscriberListResponse;
import com.example.kbuddy_backend.admin.dto.response.AdminSubscriberStatsResponse;
import com.example.kbuddy_backend.blog.repository.BlogReportRepository;
import com.example.kbuddy_backend.blog.repository.BlogReportSummary;
import com.example.kbuddy_backend.qna.repository.QnaReportRepository;
import com.example.kbuddy_backend.qna.repository.QnaReportSummary;
import com.example.kbuddy_backend.user.constant.Gender;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BlogReportRepository blogReportRepository;
    @Mock
    private QnaReportRepository qnaReportRepository;

    private AdminDashboardService adminDashboardService;

    @BeforeEach
    void setUp() {
        adminDashboardService = new AdminDashboardService(userRepository, blogReportRepository, qnaReportRepository);
    }

    @DisplayName("구독자 통계 조회 시 전체·성별 수를 반환한다")
    @Test
    void getSubscriberStats() {
        given(userRepository.count()).willReturn(10L);
        given(userRepository.countByGender(Gender.M)).willReturn(6L);
        given(userRepository.countByGender(Gender.F)).willReturn(4L);

        AdminSubscriberStatsResponse response = adminDashboardService.getSubscriberStats();

        assertThat(response.getTotalSubscribers()).isEqualTo(10);
        assertThat(response.getMaleSubscribers()).isEqualTo(6);
        assertThat(response.getFemaleSubscribers()).isEqualTo(4);
    }

    @DisplayName("구독자 목록 조회 시 페이지 정보와 사용자 정보가 매핑된다")
    @Test
    void getSubscribers() {
        User user = User.builder()
                .username("tester")
                .email("tester@example.com")
                .gender(Gender.M)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "isActive", true);
        ReflectionTestUtils.setField(user, "createdDate", LocalDateTime.now());

        PageRequest pageable = PageRequest.of(0, 20);
        given(userRepository.findAll(any()))
                .willReturn(new PageImpl<>(List.of(user), pageable, 1));

        AdminSubscriberListResponse response = adminDashboardService.getSubscribers(pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getSubscribers()).hasSize(1);
        assertThat(response.getSubscribers().get(0).getUsername()).isEqualTo("tester");
    }

    @DisplayName("신고된 게시물 목록 조회 시 블로그와 QnA가 함께 포함된다")
    @Test
    void getReportedPosts() {
        BlogReportSummary blogSummary = mockBlogSummary(1L, "blog title", 10L, "blogger", "blogger@mail.com", 3L);
        QnaReportSummary qnaSummary = mockQnaSummary(2L, "qna title", 20L, "qnaer", "qnaer@mail.com", 2L);

        given(blogReportRepository.findReportSummaries()).willReturn(List.of(blogSummary));
        given(qnaReportRepository.findReportSummaries()).willReturn(List.of(qnaSummary));

        AdminReportedPostsResponse response = adminDashboardService.getReportedPosts();

        assertThat(response.getTotalPosts()).isEqualTo(2);
        assertThat(response.getPosts())
                .anyMatch(post -> post.getPostType() == AdminPostType.BLOG && post.getReportCount() == 3);
        assertThat(response.getPosts())
                .anyMatch(post -> post.getPostType() == AdminPostType.QNA && post.getReportCount() == 2);
    }

    @DisplayName("신고된 사용자 목록 조회 시 사용자별 신고 수가 합산된다")
    @Test
    void getReportedUsers() {
        BlogReportSummary blogSummary = mockBlogSummary(1L, "blog title", 10L, "reportee", "user@mail.com", 3L);
        QnaReportSummary qnaSummary = mockQnaSummary(2L, "qna title", 10L, "reportee", "user@mail.com", 2L);

        given(blogReportRepository.findReportSummaries()).willReturn(List.of(blogSummary));
        given(qnaReportRepository.findReportSummaries()).willReturn(List.of(qnaSummary));

        AdminReportedUsersResponse response = adminDashboardService.getReportedUsers();

        assertThat(response.getTotalUsers()).isEqualTo(1);
        assertThat(response.getUsers().get(0).getTotalReports()).isEqualTo(5);
        assertThat(response.getUsers().get(0).getPosts()).hasSize(2);
    }

    private BlogReportSummary mockBlogSummary(Long blogId, String title, Long writerId, String username, String email, Long count) {
        return new BlogReportSummary() {
            @Override
            public Long getBlogId() {
                return blogId;
            }

            @Override
            public String getBlogTitle() {
                return title;
            }

            @Override
            public Long getWriterId() {
                return writerId;
            }

            @Override
            public String getWriterUsername() {
                return username;
            }

            @Override
            public String getWriterEmail() {
                return email;
            }

            @Override
            public Long getReportCount() {
                return count;
            }
        };
    }

    private QnaReportSummary mockQnaSummary(Long qnaId, String title, Long writerId, String username, String email, Long count) {
        return new QnaReportSummary() {
            @Override
            public Long getQnaId() {
                return qnaId;
            }

            @Override
            public String getQnaTitle() {
                return title;
            }

            @Override
            public Long getWriterId() {
                return writerId;
            }

            @Override
            public String getWriterUsername() {
                return username;
            }

            @Override
            public String getWriterEmail() {
                return email;
            }

            @Override
            public Long getReportCount() {
                return count;
            }
        };
    }
}

