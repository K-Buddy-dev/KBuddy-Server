package com.example.kbuddy_backend.user.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.kbuddy_backend.common.WebMVCTest;
import com.example.kbuddy_backend.user.constant.Country;
import com.example.kbuddy_backend.user.constant.Gender;
import com.example.kbuddy_backend.user.dto.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserAuthControllerTest extends WebMVCTest {


    @DisplayName("로그인 성공 테스트")
    @Test
    void loginSuccess() throws Exception {
        //given
        RegisterRequest registerRequest = RegisterRequest.of("test", "test", "test","test","test", Country.KOREA,
                Gender.M);

        //when
        
        //then
        mockMvc.perform(post("/kbuddy/v1/user/auth/login").content(objectMapper.writeValueAsString(registerRequest))
                .contentType(APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk());
     }
}