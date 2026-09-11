package com.example.kbuddy_backend.common.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.kbuddy_backend.auth.token.JwtTokenProvider;
import com.example.kbuddy_backend.blog.service.BlogService;
import com.example.kbuddy_backend.common.SecurityTest;
import com.example.kbuddy_backend.common.WebMVCTest;
import com.example.kbuddy_backend.fixtures.UserFixtures;
import com.example.kbuddy_backend.livechat.entity.CounselorInquiry;
import com.example.kbuddy_backend.livechat.service.CounselorInquiryService;
import com.example.kbuddy_backend.qna.service.QnaService;
import com.example.kbuddy_backend.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * 비로그인(게스트) 사용자의 조회 허용 범위를 검증한다.
 *
 * 게스트에게 열어주는 범위: 블로그/Q&A 목록·상세, 상담사 목록·상세·예약가능시간·리뷰.
 * 문의글과 본인 전용 조회는 로그인 이후로 유지한다.
 */
@SecurityTest
class GuestAccessTest extends WebMVCTest {

    private static final String COUNSELOR_ID = "11111111-1111-1111-1111-111111111111";

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private BlogService blogService;

    @MockBean
    private QnaService qnaService;

    @MockBean
    private CounselorInquiryService counselorInquiryService;

    @Nested
    @DisplayName("게스트에게 열어준 조회 API")
    class PublicReadApi {

        @DisplayName("게스트도 블로그 목록을 조회할 수 있다.")
        @Test
        void getAllBlogAsGuest() throws Exception {
            mockMvc.perform(get("/kbuddy/v1/blog").param("size", "10"))
                    .andExpect(status().isOk());
        }

        @DisplayName("게스트도 블로그 상세를 조회할 수 있다.")
        @Test
        void getBlogAsGuest() throws Exception {
            mockMvc.perform(get("/kbuddy/v1/blog/1"))
                    .andExpect(status().isOk());
        }

        @DisplayName("게스트도 Q&A 목록을 조회할 수 있다.")
        @Test
        void getAllQnaAsGuest() throws Exception {
            mockMvc.perform(get("/kbuddy/v1/qna").param("size", "10"))
                    .andExpect(status().isOk());
        }

        @DisplayName("게스트도 Q&A 상세를 조회할 수 있다.")
        @Test
        void getQnaAsGuest() throws Exception {
            mockMvc.perform(get("/kbuddy/v1/qna/1"))
                    .andExpect(status().isOk());
        }

        @DisplayName("게스트도 상담사 목록을 조회할 수 있다.")
        @Test
        void getCounselorsAsGuest() throws Exception {
            mockMvc.perform(get("/kbuddy/v1/counselor"))
                    .andExpect(status().isOk());
        }

        @DisplayName("게스트도 상담사 상세를 조회할 수 있다.")
        @Test
        void getCounselorAsGuest() throws Exception {
            mockMvc.perform(get("/kbuddy/v1/counselor/" + COUNSELOR_ID))
                    .andExpect(status().isOk());
        }

        @DisplayName("게스트도 상담 가능 시간을 조회할 수 있다.")
        @Test
        void getAvailabilityAsGuest() throws Exception {
            mockMvc.perform(get("/kbuddy/v1/counselor/" + COUNSELOR_ID + "/availability")
                            .param("year", "2026")
                            .param("month", "8"))
                    .andExpect(status().isOk());
        }

        @DisplayName("게스트도 상담사 리뷰를 조회할 수 있다.")
        @Test
        void getReviewsAsGuest() throws Exception {
            mockMvc.perform(get("/kbuddy/v1/counselor/" + COUNSELOR_ID + "/review"))
                    .andExpect(status().isOk());
        }

        @DisplayName("게스트도 문의글 목록을 조회할 수 있다.")
        @Test
        void getInquiriesAsGuest() throws Exception {
            given(counselorInquiryService.getInquiries(isNull(), anyString(), any()))
                    .willReturn(Page.empty());

            mockMvc.perform(get("/kbuddy/v1/counselor/" + COUNSELOR_ID + "/inquiry"))
                    .andExpect(status().isOk());
        }

        @DisplayName("게스트도 공개 문의글 상세를 조회할 수 있다.")
        @Test
        void getPublicInquiryDetailAsGuest() throws Exception {
            CounselorInquiry inquiry = CounselorInquiry.builder()
                    .counselor(UserFixtures.createUser())
                    .writer(UserFixtures.createUser())
                    .title("title")
                    .content("content")
                    .isSecret(false)
                    .build();
            given(counselorInquiryService.getInquiryDetail(isNull(), anyLong())).willReturn(inquiry);

            mockMvc.perform(get("/kbuddy/v1/counselor/" + COUNSELOR_ID + "/inquiry/1"))
                    .andExpect(status().isOk());
        }

        @DisplayName("게스트 조회 시 CurrentUser에는 null이 주입된다.")
        @Test
        void currentUserIsNullForGuest() throws Exception {
            mockMvc.perform(get("/kbuddy/v1/blog").param("size", "10"))
                    .andExpect(status().isOk());

            verify(blogService).getAllBlog(anyInt(), isNull(), anyString(), isNull(), isNull(), isNull(), isNull());
        }
    }

    @Nested
    @DisplayName("게스트에게 열지 않은 API")
    class ProtectedApi {

        @DisplayName("본인 전용 상담사 프로필은 게스트가 조회할 수 없다.")
        @Test
        void getMyProfileAsGuest() throws Exception {
            mockMvc.perform(get("/kbuddy/v1/counselor/me"))
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("비밀 문의글은 게스트가 조회할 수 없다.")
        @Test
        void getSecretInquiryDetailAsGuest() throws Exception {
            given(counselorInquiryService.getInquiryDetail(isNull(), anyLong()))
                    .willThrow(new AccessDeniedException("이 문의글을 볼 권한이 없습니다"));

            mockMvc.perform(get("/kbuddy/v1/counselor/" + COUNSELOR_ID + "/inquiry/1"))
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("게스트는 문의글을 작성할 수 없다.")
        @Test
        void createInquiryAsGuest() throws Exception {
            mockMvc.perform(post("/kbuddy/v1/counselor/" + COUNSELOR_ID + "/inquiry")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"t\",\"content\":\"c\",\"secret\":false}"))
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("게스트는 블로그 글을 작성할 수 없다.")
        @Test
        void saveBlogAsGuest() throws Exception {
            mockMvc.perform(post("/kbuddy/v1/blog").contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("게스트는 좋아요를 누를 수 없다.")
        @Test
        void plusHeartAsGuest() throws Exception {
            mockMvc.perform(post("/kbuddy/v1/blog/1/hearts"))
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("게스트는 북마크를 추가할 수 없다.")
        @Test
        void addBookmarkAsGuest() throws Exception {
            mockMvc.perform(post("/kbuddy/v1/blog/1/bookmark"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("접근 거부 응답 코드")
    class AccessDenied {

        /**
         * 임시저장 글 접근은 서비스 계층에서 AccessDeniedException으로 막힌다.
         * catch-all 핸들러에 삼켜져 500이 되면 클라이언트가 "로그인 필요"를 구분할 수 없다.
         */
        @DisplayName("게스트가 임시저장 글을 조회하면 401을 반환한다.")
        @Test
        void draftAccessByGuestReturnsUnauthorized() throws Exception {
            given(blogService.getBlog(anyLong(), isNull()))
                    .willThrow(new AccessDeniedException("로그인이 필요합니다."));

            mockMvc.perform(get("/kbuddy/v1/blog/1"))
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("로그인 사용자의 접근 거부는 403을 반환한다.")
        @Test
        @WithMockUser(username = "123", roles = "USER")
        void accessDeniedForLoggedInUserReturnsForbidden() throws Exception {
            User user = UserFixtures.createUser();
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(blogService.getBlog(anyLong(), any(User.class)))
                    .willThrow(new AccessDeniedException("임시 저장된 글은 작성자만 조회할 수 있습니다."));

            mockMvc.perform(get("/kbuddy/v1/blog/1"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("무효한 토큰 처리")
    class InvalidToken {

        /**
         * 게스트 조회가 허용된 경로라도, 만료된 토큰을 들고 온 요청을 익명으로 강등하면
         * 클라이언트의 토큰 재발급 트리거가 사라져 로그인 상태가 조용히 풀린다.
         */
        @DisplayName("만료된 토큰으로 게스트 허용 경로를 조회하면 401을 반환한다.")
        @Test
        void expiredTokenOnPublicPathIsRejected() throws Exception {
            given(jwtTokenProvider.validateToken(anyString())).willReturn(false);

            mockMvc.perform(get("/kbuddy/v1/blog")
                            .param("size", "10")
                            .header("Authorization", "Bearer expiredToken"))
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("토큰 재발급 경로는 만료된 토큰이 실려와도 거부하지 않는다.")
        @Test
        void expiredTokenOnRefreshPathIsNotRejected() throws Exception {
            given(jwtTokenProvider.validateToken(anyString())).willReturn(false);

            mockMvc.perform(get("/kbuddy/v1/auth/accessToken")
                            .header("Authorization", "Bearer expiredToken"))
                    .andExpect(status().isOk());
        }

        /**
         * 관리자 클라이언트도 액세스 토큰을 전역 헤더에 보관하므로,
         * 만료 후 재발급/재로그인 요청에 만료 토큰이 그대로 실려온다.
         * 이 경로를 거부하면 관리자가 로그인 자체를 할 수 없게 된다.
         */
        @DisplayName("관리자 토큰 재발급 경로는 만료된 토큰이 실려와도 거부하지 않는다.")
        @Test
        void expiredTokenOnAdminRefreshPathIsNotRejected() throws Exception {
            given(jwtTokenProvider.validateToken(anyString())).willReturn(false);

            mockMvc.perform(get("/kbuddy/v1/admin/refresh")
                            .header("Authorization", "Bearer expiredToken"))
                    .andExpect(status().is(not(401)));
        }

        @DisplayName("관리자 로그인 경로는 만료된 토큰이 실려와도 거부하지 않는다.")
        @Test
        void expiredTokenOnAdminLoginPathIsNotRejected() throws Exception {
            given(jwtTokenProvider.validateToken(anyString())).willReturn(false);

            mockMvc.perform(post("/kbuddy/v1/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"a@b.c\",\"password\":\"pw\"}")
                            .header("Authorization", "Bearer expiredToken"))
                    .andExpect(status().is(not(401)));
        }

        private org.hamcrest.Matcher<Integer> not(int status) {
            return org.hamcrest.Matchers.not(org.hamcrest.Matchers.is(status));
        }
    }

    /**
     * 게스트 조회가 열리면서 비로그인 사용자도 오류 응답 본문에 닿는다.
     * 내부 구조가 응답으로 새어나가지 않아야 한다.
     */
    @Nested
    @DisplayName("게스트가 받는 오류 응답")
    class GuestErrorResponse {

        @DisplayName("숫자가 아닌 게시글 ID는 500이 아니라 400으로 응답한다.")
        @Test
        void nonNumericBlogIdIsBadRequest() throws Exception {
            mockMvc.perform(get("/kbuddy/v1/blog/category"))
                    .andExpect(status().isBadRequest());
        }

        @DisplayName("500 응답 본문에 스택 트레이스를 담지 않는다.")
        @Test
        void internalErrorDoesNotExposeStackTrace() throws Exception {
            given(blogService.getBlog(anyLong(), isNull())).willThrow(new RuntimeException("boom"));

            String body = mockMvc.perform(get("/kbuddy/v1/blog/1"))
                    .andExpect(status().isInternalServerError())
                    .andReturn().getResponse().getContentAsString();

            org.assertj.core.api.Assertions.assertThat(body)
                    .doesNotContain("org.springframework")
                    .doesNotContain("com.example.kbuddy_backend")
                    .doesNotContain("boom");
        }
    }
}
