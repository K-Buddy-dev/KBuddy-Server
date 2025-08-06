package com.example.kbuddy_backend.user.controller;

import static org.springframework.http.HttpStatus.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenResponse;
import com.example.kbuddy_backend.auth.service.MailSendService;
import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.common.validate.DateValidator;
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
import com.example.kbuddy_backend.user.dto.response.AppleLoginResponse;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.exception.DuplicateEmailException;
import com.example.kbuddy_backend.user.exception.DuplicateUserIdException;
import com.example.kbuddy_backend.user.repository.UserRepository;
import com.example.kbuddy_backend.user.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbuddy/v1/auth")
@Tag(name = "Auth API", description = "인증 API 목록")
public class UserAuthController {

    private final UserAuthService userAuthService;
    private final MailSendService mailService;
    private final UserRepository userRepository;
    @Value("${spring.security.jwt.refresh-token-expiration}")
    private int refreshTokenExpiration;

    @Operation(summary = "아이디/패스워드 회원 가입", description = "아이디/패스워드 기반 회원가입을 합니다.")
    @PostMapping("/register")
    public ResponseEntity<AccessTokenResponse> register(
            @Valid @RequestBody final RegisterRequest registerRequest, HttpServletResponse response) {
        DateValidator.isValidDate(registerRequest.birthDate());
        AccessTokenAndRefreshTokenResponse token = userAuthService.register(registerRequest);
        System.out.println("token = " + token);
        setRefreshTokenInCookie(response, token);
        return ResponseEntity.status(CREATED).body(AccessTokenResponse.of(token.accessToken(),token.accessTokenExpireTime()));
    }



    @Operation(summary = "OAuth 회원가입", description = "OAuth(KAKAO, GOOGLE, APPLE) 기반 회원가입을 합니다.")
    @PostMapping("/oauth/register")
    public ResponseEntity<AccessTokenResponse> oAuthRegister(
            @RequestBody @Valid final OAuthRegisterRequest registerRequest, HttpServletResponse response) {
        DateValidator.isValidDate(registerRequest.birthDate());
        AccessTokenAndRefreshTokenResponse token = userAuthService.oAuthRegister(registerRequest);
        setRefreshTokenInCookie(response, token);
        return ResponseEntity.status(CREATED).body(AccessTokenResponse.of(token.accessToken(),token.accessTokenExpireTime()));
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
    public ResponseEntity<AccessTokenResponse> login(@RequestBody @Valid final LoginRequest loginRequest, HttpServletResponse response) {
        AccessTokenAndRefreshTokenResponse token = userAuthService.login(loginRequest);
        setRefreshTokenInCookie(response, token);
        return ResponseEntity.status(CREATED).body(AccessTokenResponse.of(token.accessToken(),token.accessTokenExpireTime()));
    }

    @Operation(summary = "OAuth 로그인", description = "OAuth를 통해 로그인합니다.ㅣ")
    @PostMapping("/oauth/login")
    public ResponseEntity<AccessTokenResponse> oAuthLogin(
            @RequestBody @Valid final OAuthLoginRequest loginRequest, HttpServletResponse response) {
        AccessTokenAndRefreshTokenResponse token = userAuthService.oAuthLogin(loginRequest);
        setRefreshTokenInCookie(response, token);
        return ResponseEntity.status(CREATED).body(AccessTokenResponse.of(token.accessToken(),token.accessTokenExpireTime()));
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
     * 250301 이메일 검사 api 분리
     * */
    @Operation(summary = "이메일 중복 체크", description = "이메일의 중복을 검사합니다.")
    @PostMapping("/email/check")
    public ResponseEntity<DefaultResponse> mailCheck(@RequestBody @Valid EmailRequest emailRequest) {

        if (userRepository.existsByEmailAndOauthCategoryIsNullAndOauthUidIsNull(emailRequest.email())) {
            throw new DuplicateEmailException();
        }

        return ResponseEntity.ok().body(DefaultResponse.of(true, "사용 가능한 이메일입니다."));
    }

    @Operation(summary = "이메일 코드 전송", description = "이메일에 인증 코드를 전송합니다.")
    @PostMapping("/email/send")
    public ResponseEntity<EmailCodeResponse> mailSend(@RequestBody @Valid EmailRequest emailRequest) {

        if (userRepository.existsByEmailAndOauthCategoryIsNullAndOauthUidIsNull(emailRequest.email())) {
            throw new DuplicateEmailException();
        }

        String code = mailService.joinEmail(emailRequest.email());
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

    private void setRefreshTokenInCookie(HttpServletResponse response, AccessTokenAndRefreshTokenResponse token) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", token.refreshToken());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(refreshTokenExpiration);
        response.addCookie(refreshTokenCookie);
    }

    //로그아웃
    @Operation(summary = "로그아웃", description = "로그아웃을 합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Parameter(hidden = true) @CurrentUser User user, HttpServletResponse response) {
        //쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setSecure(true);
        response.addCookie(refreshTokenCookie); // 응답에 쿠키 포함!

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "계정 삭제", description = "사용자 계정을 삭제(비활성화)합니다.")
    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(
            @Parameter(hidden = true) @CurrentUser User user, 
            HttpServletResponse response) {
        // 계정 삭제 처리
        userAuthService.deleteAccount(user);
        
        // 로그아웃 처리 (쿠키 삭제)
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setSecure(true);
        response.addCookie(refreshTokenCookie);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Apple 로그인 콜백", description = "Apple에서 전송하는 로그인 콜백을 처리하고 302 리다이렉트합니다.")
    @PostMapping(value = "/apple/callback", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Void> appleCallback(
            @RequestParam("code") String code,
            @RequestParam("id_token") String idToken,
            @RequestParam(value = "user", required = false) String user,
            HttpServletResponse response) {
        AppleLoginResponse appleResponse = userAuthService.handleAppleLogin(idToken, user);
        
        // 토큰을 쿠키에 설정 (기존 사용자인 경우에만)
        if (!appleResponse.accessToken().isEmpty()) {
            // Refresh token을 쿠키에 설정
            Cookie refreshTokenCookie = new Cookie("refreshToken", appleResponse.refreshToken());
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setMaxAge(refreshTokenExpiration);
            response.addCookie(refreshTokenCookie);
        }
        
        // 302 리다이렉트 응답 생성
        String frontendDomain = "https://k-buddy.kr";
        String redirectUrl = frontendDomain + "/oauth/apple-redirect?accessToken=" + appleResponse.accessToken() + "&isNew=" + appleResponse.isNew()
                + "&email=" + appleResponse.email() + "&oAuthUid=" + appleResponse.oAuthUid()
                + "&firstName=" + appleResponse.firstName() + "&lastName=" + appleResponse.lastName();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .build();
    }
}