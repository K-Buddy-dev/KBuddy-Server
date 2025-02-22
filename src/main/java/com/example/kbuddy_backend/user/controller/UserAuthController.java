package com.example.kbuddy_backend.user.controller;

import static org.springframework.http.HttpStatus.*;

import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.auth.service.MailSendService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.user.dto.request.EmailCheckRequest;
import com.example.kbuddy_backend.user.dto.request.EmailRequest;
import com.example.kbuddy_backend.user.dto.request.LoginRequest;
import com.example.kbuddy_backend.user.dto.request.OAuthLoginRequest;
import com.example.kbuddy_backend.user.dto.request.OAuthRegisterRequest;
import com.example.kbuddy_backend.user.dto.request.PasswordRequest;
import com.example.kbuddy_backend.user.dto.request.RegisterRequest;
import com.example.kbuddy_backend.user.dto.request.UserNameCheckRequest;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;
import com.example.kbuddy_backend.user.dto.response.EmailCodeResponse;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.exception.DuplicateEmailException;
import com.example.kbuddy_backend.user.exception.DuplicateUserIdException;
import com.example.kbuddy_backend.user.repository.UserRepository;
import com.example.kbuddy_backend.user.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/auth")
@Tag(name = "Auth API", description = "인증 API 목록")
public class UserAuthController {

    private final UserAuthService userAuthService;
    private final MailSendService mailService;
    private final UserRepository userRepository;

    @Operation(summary = "아이디/패스워드 회원 가입", description = "아이디/패스워드 기반 회원가입을 합니다.")
    @PostMapping("/register")
    public ResponseEntity<AccessTokenAndRefreshTokenResponse> register(
            @Valid @RequestBody final RegisterRequest registerRequest) {

        AccessTokenAndRefreshTokenResponse token = userAuthService.register(registerRequest);
        return ResponseEntity.status(CREATED).body(token);
    }

    @Operation(summary = "OAuth 회원가입", description = "OAuth(KAKAO, GOOGLE, APPLE) 기반 회원가입을 합니다.")
    @PostMapping("/oauth/register")
    public ResponseEntity<AccessTokenAndRefreshTokenResponse> oAuthRegister(
            @RequestBody @Valid final OAuthRegisterRequest registerRequest) {

        AccessTokenAndRefreshTokenResponse token = userAuthService.oAuthRegister(registerRequest);
        return ResponseEntity.status(CREATED).body(token);
    }

    @Operation(summary = "OAuth 회원가입 체크", description = "OAuth로 회원가입한 내역이 있는지 검사합니다.")
    @PostMapping("/oauth/check")
    public ResponseEntity<DefaultResponse> checkOAuthUser(
            @RequestBody @Valid final OAuthLoginRequest request) {
        if (userAuthService.checkOAuthUser(request)) {
            return ResponseEntity.ok().body(DefaultResponse.of(true, "가입된 내역이 있습니다."));
        }
        return ResponseEntity.ok().body(DefaultResponse.of(false, "가입된 내역이 없습니다."));
    }

    @Operation(summary = "아이디/패스워드 로그인", description = "아이디/패스워드 기반 로그인을 합니다.")
    @PostMapping("/login")
    public ResponseEntity<AccessTokenAndRefreshTokenResponse> login(@RequestBody @Valid final LoginRequest loginRequest) {
        AccessTokenAndRefreshTokenResponse token = userAuthService.login(loginRequest);
        return ResponseEntity.ok().body(token);
    }

    @Operation(summary = "OAuth 로그인", description = "OAuth를 통해 로그인합니다.ㅣ")
    @PostMapping("/oauth/login")
    public ResponseEntity<AccessTokenAndRefreshTokenResponse> oAuthLogin(
            @RequestBody @Valid final OAuthLoginRequest loginRequest) {
        AccessTokenAndRefreshTokenResponse token = userAuthService.oAuthLogin(loginRequest);
        return ResponseEntity.ok().body(token);
    }

    @Operation(summary = "비밀번호 변경", description = "아이디/패스워드 기반 회원가입한 사용자의 비밀번호를 변경합니다.")
    @PostMapping("/password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid final PasswordRequest passwordRequest,
                                                @Parameter(hidden = true) @CurrentUser
                                                User user) {
        userAuthService.resetPassword(passwordRequest, user);
        return ResponseEntity.ok().body("비밀번호 변경 성공");
    }

    @Operation(summary = "사용자 아이디 중복 체크", description = "아이디/패스워드 기반 회원가입하는 사용자의 사용자 아이디의 중복여부를 체크합니다.")
    @PostMapping("/userId/check")
    public ResponseEntity<DefaultResponse> checkUserId(@RequestBody @Valid UserNameCheckRequest userNameCheckRequest) {
        if (userRepository.existsByUsername(userNameCheckRequest.userId())) {
            throw new DuplicateUserIdException();
        }
        return ResponseEntity.ok().body(DefaultResponse.of(true, "사용 가능한 사용자 아이디입니다."));
    }

    /**
     * 사용 가능한 이메일인지 검사 -> 이메일 코드 전송
     * */
    @Operation(summary = "이메일 코드 전송", description = "이메일에 인증 코드를 전송합니다.")
    @PostMapping("/email/send")
    public ResponseEntity<EmailCodeResponse> mailSend(@RequestBody @Valid EmailRequest emailRequest) {

        if (userRepository.existsByEmailAndOauthCategoryIsNullAndOauthUidIsNull(emailRequest.email())) {
            throw new DuplicateEmailException();
        }

        int code = mailService.joinEmail(emailRequest.email());
        return ResponseEntity.ok().body(new EmailCodeResponse(emailRequest.email(), code));
    }

    //이메일 코드 인증
    @Operation(summary = "이메일 코드 검사", description = "이메일로 전송된 코드를 검사합니다.")
    @PostMapping("/email/code")
    public ResponseEntity<DefaultResponse> authCheck(@RequestBody @Valid EmailCheckRequest emailCheckRequest) {
        boolean checked = mailService.CheckAuthNum(emailCheckRequest.email(), emailCheckRequest.code());
        if (checked) {
            return ResponseEntity.ok().body(DefaultResponse.of(true, "인증 성공"));
        } else {
            return ResponseEntity.status(UNAUTHORIZED).body(DefaultResponse.of(false, "인증 실패"));
        }
    }

    //테스트 api
    @GetMapping("/authentication")
    public ResponseEntity<Authentication>
    getUserAuthentication(Authentication authentication) {
        return ResponseEntity.ok().body(authentication);
    }

}