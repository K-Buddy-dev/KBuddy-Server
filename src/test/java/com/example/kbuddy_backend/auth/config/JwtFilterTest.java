package com.example.kbuddy_backend.auth.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.kbuddy_backend.auth.token.JwtTokenProvider;
import com.example.kbuddy_backend.common.SecurityTest;
import com.example.kbuddy_backend.common.WebMVCTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

@SecurityTest
class JwtFilterTest extends WebMVCTest {

    @MockBean
    private JwtTokenProvider tokenProvider;

    @DisplayName("필터에서 유효한 토큰을 정상적으로 처리하는지 확인한다.")
    @Test
    void checkValidToken() throws Exception {
        //given
        final List<GrantedAuthority> grantedAuthorities = List.of(() -> "ROLE_USER");
        final UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("email", "password", grantedAuthorities);

        given(tokenProvider.validateToken(anyString())).willReturn(true);
        given(tokenProvider.getAuthentication(anyString())).willReturn(authentication);

        //then
        mockMvc.perform(get("/kbuddy/v1/user/auth/authentication")
                        .header("Authorization", "Bearer validToken"))
                .andExpect(status().isOk());
    }

    @DisplayName("필터에서 유효하지 않은 토큰을 처리하는지 확인한다.")
    @Test
    void checkInValidToken() throws Exception {
        //given
        given(tokenProvider.validateToken(anyString())).willReturn(false);

        //when

        //then
        mockMvc.perform(get("/kbuddy/v1/user/auth/authentication")
                        .header("Authorization", "Bearer invalidToken"))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("헤더에 토큰이 없는 경우를 확인한다.")
    @Test
    void checkNullToken() throws Exception {

        //then
        mockMvc.perform(get("/kbuddy/v1/user/auth/authentication"))
                .andExpect(status().isUnauthorized());
    }
}