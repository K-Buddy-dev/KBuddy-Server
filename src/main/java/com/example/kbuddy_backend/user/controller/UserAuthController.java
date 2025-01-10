package com.example.kbuddy_backend.user.controller;

import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.auth.service.MailSendService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.common.exception.BadRequestException;
import com.example.kbuddy_backend.user.dto.request.EmailCheckRequest;
import com.example.kbuddy_backend.user.dto.request.EmailRequest;
import com.example.kbuddy_backend.user.dto.request.LoginRequest;
import com.example.kbuddy_backend.user.dto.request.OAuthLoginRequest;
import com.example.kbuddy_backend.user.dto.request.OAuthRegisterRequest;
import com.example.kbuddy_backend.user.dto.request.PasswordRequest;
import com.example.kbuddy_backend.user.dto.request.RegisterRequest;
import com.example.kbuddy_backend.user.dto.response.DefaultResponse;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.repository.UserRepository;
import com.example.kbuddy_backend.user.service.UserAuthService;
import com.example.kbuddy_backend.user.service.UserService;
import jakarta.validation.Valid;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/user/auth")
public class UserAuthController {

    private final UserAuthService userAuthService;
    private final UserService userService;
    private final MailSendService mailService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AccessTokenAndRefreshTokenResponse> register(
            @Valid @RequestBody final RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        AccessTokenAndRefreshTokenResponse token = userAuthService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    @PostMapping("/oauth/register")
    public ResponseEntity<AccessTokenAndRefreshTokenResponse> oAuthRegister(
            @RequestBody final OAuthRegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }

        AccessTokenAndRefreshTokenResponse token = userAuthService.oAuthRegister(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    @PostMapping("/oauth/check")
    public ResponseEntity<DefaultResponse> checkOAuthUser(
            @RequestBody final OAuthLoginRequest request) {
        if (userAuthService.checkOAuthUser(request)) {
            return ResponseEntity.ok().body(DefaultResponse.of(true, "가입된 내역이 있습니다."));
        }
        return ResponseEntity.ok().body(DefaultResponse.of(false, "가입된 내역이 없습니다."));
    }


    @PostMapping("/login")
    public ResponseEntity<AccessTokenAndRefreshTokenResponse> login(@RequestBody final LoginRequest loginRequest) {
        AccessTokenAndRefreshTokenResponse token = userAuthService.login(loginRequest);
        return ResponseEntity.ok().body(token);
    }

    @PostMapping("/oauth/login")
    public ResponseEntity<AccessTokenAndRefreshTokenResponse> oAuthLogin(
            @RequestBody final OAuthLoginRequest loginRequest) {
        AccessTokenAndRefreshTokenResponse token = userAuthService.oAuthLogin(loginRequest);

        return ResponseEntity.ok().body(token);
    }

    @PostMapping("/password")
    public ResponseEntity<String> resetPassword(@RequestBody final PasswordRequest passwordRequest,
                                                @CurrentUser
                                                User user) {
        userAuthService.resetPassword(passwordRequest, user);
        return ResponseEntity.ok().body("비밀번호 변경 성공");

    }

    //토큰 불필요
    @PostMapping("/email/check")
    public ResponseEntity<DefaultResponse> checkEmail(@Valid @RequestBody final EmailRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {

            return ResponseEntity.ok().body(DefaultResponse.of(false, "이메일이 존재합니다."));
        }
        return ResponseEntity.ok().body(DefaultResponse.of(true, "사용가능한 이메일입니다."));
    }


    //이메일 코드 전송
    @PostMapping("/email/send")
    public ResponseEntity<String> mailSend(@RequestBody @Valid EmailRequest emailRequest) {
        String code = mailService.joinEmail(emailRequest.email());
        return ResponseEntity.ok().body(code);
    }

    //이메일 코드 인증
    @PostMapping("/email/code")
    public ResponseEntity<DefaultResponse> authCheck(@RequestBody @Valid EmailCheckRequest emailCheckRequest) {
        boolean checked = mailService.CheckAuthNum(emailCheckRequest.email(), emailCheckRequest.code());
        if (checked) {
            return ResponseEntity.ok().body(DefaultResponse.of(true, "인증 성공"));
        } else {
            return ResponseEntity.ok().body(DefaultResponse.of(false, "인증 실패"));
        }
    }

    //테스트 api
    @GetMapping("/authentication")
    public ResponseEntity<Authentication>
    getUserAuthentication(Authentication authentication) {

        return ResponseEntity.ok().body(authentication);
    }

}