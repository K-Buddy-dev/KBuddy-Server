package com.example.kbuddy_backend.common;

import com.example.kbuddy_backend.auth.token.JwtTokenProvider;
import com.example.kbuddy_backend.auth.token.TokenProvider;
import com.example.kbuddy_backend.common.config.SecurityConfig;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(includeFilters = @Filter(type = FilterType.ANNOTATION, classes = RestController.class),
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
@Import({JwtTokenProvider.class, MockedServiceClassBeanRegister.class})
public abstract class WebMVCTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected TokenProvider tokenProvider;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected UserRepository userRepository;
}
