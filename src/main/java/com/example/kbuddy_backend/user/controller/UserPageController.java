package com.example.kbuddy_backend.user.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.user.dto.request.UserBioRequest;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;
import com.example.kbuddy_backend.user.dto.response.UserResponse;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.service.UserService;
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
public class UserPageController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getUser(@CurrentUser User user) {

        UserResponse findUser = userService.getUser(user);
        return ResponseEntity.ok().body(findUser);
    }

    @PostMapping("/bio")
    public ResponseEntity<String> saveUserBio(@RequestBody UserBioRequest request, @CurrentUser User user) {
        userService.saveUserBio(request, user);
        return ResponseEntity.ok().body("성공적으로 저장되었습니다.");
    }
}
