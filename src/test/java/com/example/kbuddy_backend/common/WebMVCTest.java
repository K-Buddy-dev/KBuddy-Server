package com.example.kbuddy_backend.common;

import com.example.kbuddy_backend.auth.token.JwtTokenProvider;
import com.example.kbuddy_backend.auth.token.TokenProvider;
import com.example.kbuddy_backend.common.config.SecurityConfig;
import com.example.kbuddy_backend.livechat.service.CounselorProfileService;
import com.example.kbuddy_backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(includeFilters = @Filter(type = FilterType.ANNOTATION, classes = RestController.class), excludeAutoConfiguration = {
        SecurityAutoConfiguration.class, WebSecurityConfiguration.class }, excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) })
@Import({ JwtTokenProvider.class, MockedServiceClassBeanRegister.class })
public abstract class WebMVCTest {

    /*
     * CounselorController가 CounselorProfileService를 요구하므로
     * Service를 완전히 제거하면 의존성 주입 오류가 발생합니다.
     *
     * Spring Boot의 @MockBean으로 대체하면 Controller가 요구하는 Bean은 존재하지만,
     * 실제 Service의 @PersistenceContext EntityManager는 주입하지 않습니다.
     */
    @MockBean
    protected CounselorProfileService counselorProfileService;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected TokenProvider tokenProvider;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected UserRepository userRepository;

    @MockBean
    protected com.example.kbuddy_backend.admin.config.AdminCredentialsProperties adminCredentialsProperties;
}
