package com.example.kbuddy_backend.user.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.kbuddy_backend.common.WebMVCTest;
import com.example.kbuddy_backend.user.constant.Country;
import com.example.kbuddy_backend.user.constant.Gender;
import com.example.kbuddy_backend.user.dto.request.LoginRequest;
import com.example.kbuddy_backend.user.dto.request.RegisterRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// class UserAuthControllerTest extends WebMVCTest {
//
//
//     @DisplayName("로그인 성공 테스트")
//     @Test
//     void loginSuccess() throws Exception {
//         //given
//
//         LoginRequest loginRequest = LoginRequest.of("johnhuh1", "kbuddy0188!");
//         RegisterRequest registerRequest = RegisterRequest.of(
//             "johnhuh1",
//             "johnhuh@example.com",
//             "kbuddy0188!",
//             "John",
//             "Huh",
//             Country.US,
//             Gender.F,
//             "000724"
//         );
//
//         //when
//         mockMvc.perform(post("/kbuddy/v1/auth/register")
//                 .content(objectMapper.writeValueAsString(registerRequest))
//                 .contentType(APPLICATION_JSON))
//                 .andDo(print())
//             .andExpect(status().isCreated());
//
//         //then
//         mockMvc.perform(post("/kbuddy/v1/auth/login").content(objectMapper.writeValueAsString(loginRequest))
//                 .contentType(APPLICATION_JSON)).andDo(print())
//                 .andDo(print())
//                 .andExpect(status().isOk());
//      }
// }