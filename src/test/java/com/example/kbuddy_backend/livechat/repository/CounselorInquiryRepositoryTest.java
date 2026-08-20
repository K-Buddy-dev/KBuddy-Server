package com.example.kbuddy_backend.livechat.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.kbuddy_backend.common.IntegrationTest;
import com.example.kbuddy_backend.livechat.entity.CounselorInquiry;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비밀 문의글이 비로그인 사용자에게 노출되지 않는지 실제 DB 쿼리로 검증한다.
 */
@Transactional
class CounselorInquiryRepositoryTest extends IntegrationTest {

    @Autowired
    private CounselorInquiryRepository inquiryRepository;

    @Autowired
    private UserRepository userRepository;

    private User counselor;
    private User writer;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        counselor = userRepository.save(User.builder()
                .email("counselor@k-buddy.kr").username("counselor").password("pw").build());
        writer = userRepository.save(User.builder()
                .email("writer@k-buddy.kr").username("writer").password("pw").build());
        pageable = PageRequest.of(0, 20);

        inquiryRepository.save(CounselorInquiry.builder()
                .counselor(counselor).writer(writer)
                .title("공개 문의").content("공개 내용").isSecret(false).build());
        inquiryRepository.save(CounselorInquiry.builder()
                .counselor(counselor).writer(writer)
                .title("비밀 문의").content("비밀 내용").isSecret(true).build());
    }

    @DisplayName("비로그인 사용자에게는 공개 문의글만 조회된다.")
    @Test
    void findPublicInquiriesExcludesSecret() {
        List<CounselorInquiry> result =
                inquiryRepository.findPublicInquiries(counselor.getId(), pageable).getContent();

        assertEquals(1, result.size());
        assertEquals("공개 문의", result.get(0).getTitle());
        assertTrue(result.stream().noneMatch(CounselorInquiry::isSecret));
    }

    @DisplayName("작성자에게는 본인의 비밀 문의글도 조회된다.")
    @Test
    void findVisibleInquiriesIncludesOwnSecret() {
        List<CounselorInquiry> result =
                inquiryRepository.findVisibleInquiries(counselor.getId(), writer, pageable).getContent();

        assertEquals(2, result.size());
    }

    @DisplayName("제3자에게는 비밀 문의글이 조회되지 않는다.")
    @Test
    void findVisibleInquiriesExcludesOthersSecret() {
        User stranger = userRepository.save(User.builder()
                .email("stranger@k-buddy.kr").username("stranger").password("pw").build());

        List<CounselorInquiry> result =
                inquiryRepository.findVisibleInquiries(counselor.getId(), stranger, pageable).getContent();

        assertEquals(1, result.size());
        assertEquals("공개 문의", result.get(0).getTitle());
    }
}
