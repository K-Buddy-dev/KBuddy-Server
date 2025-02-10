package com.example.kbuddy_backend.user.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.user.dto.request.UserBioRequest;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;
import com.example.kbuddy_backend.user.dto.response.UserResponse;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/user")
@Tag(name = "User API", description = "사용자 API 목록")
public class UserPageController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "사용자 정보 조회", description = "JWT 토큰을 통해 사용자 정보를 조회합니다.")
    public ResponseEntity<UserResponse> getUser(@Parameter(hidden = true) @CurrentUser User user) {

        UserResponse findUser = userService.getUser(user);
        return ResponseEntity.ok().body(findUser);
    }

    @PostMapping("/bio")
    @Operation(summary = "자기소개 작성", description = "사용자 자기소개를 작성합니다.")
    public ResponseEntity<String> saveUserBio(@RequestBody UserBioRequest request,@Parameter(hidden = true) @CurrentUser User user) {
        userService.saveUserBio(request, user);
        return ResponseEntity.ok().body("성공적으로 저장되었습니다.");
    }
}
