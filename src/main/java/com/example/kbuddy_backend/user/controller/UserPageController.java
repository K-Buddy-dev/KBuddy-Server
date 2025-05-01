package com.example.kbuddy_backend.user.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.qna.dto.response.QnaPaginationResponse;
import com.example.kbuddy_backend.user.dto.request.UserBioRequest;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;
import com.example.kbuddy_backend.user.dto.response.DraftListResponse;
import com.example.kbuddy_backend.user.dto.response.UserProfileResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import com.example.kbuddy_backend.common.exception.BadRequestException;
import com.example.kbuddy_backend.s3.dto.response.S3Response;
import com.example.kbuddy_backend.user.dto.request.UserProfileUpdateRequestDto;
import org.springframework.web.bind.annotation.RequestPart;
import jakarta.validation.Valid;
import com.example.kbuddy_backend.common.advice.response.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

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

    @GetMapping("/drafts")
    @Operation(summary = "내 임시 저장 게시글 목록 조회", description = "현재 로그인한 사용자가 임시 저장한 게시글 전체 목록을 조회합니다.")
    public ResponseEntity<List<DraftListResponse>> getMyDrafts(
            @Parameter(hidden = true) @CurrentUser User user) {
        List<DraftListResponse> drafts = userService.getMyDrafts(user);
        return ResponseEntity.ok(drafts);
    }

    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "사용자 프로필 수정 (bio, 프로필 이미지)", description = "사용자의 자기소개와 프로필 이미지를 수정합니다. 수정할 필드만 요청에 포함합니다.")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @Parameter(hidden = true) @CurrentUser User currentUser,
            @Valid @RequestPart(value = "bio", required = false) UserProfileUpdateRequestDto requestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImageFile) {
        String bio = (requestDto != null) ? requestDto.bio() : null;
        UserProfileResponse userProfileResponse = userService.updateUserProfile(currentUser, bio, profileImageFile);
        return ResponseEntity.ok(userProfileResponse);
    }
}
