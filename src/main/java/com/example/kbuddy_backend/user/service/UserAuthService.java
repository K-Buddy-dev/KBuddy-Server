package com.example.kbuddy_backend.user.service;

import static com.example.kbuddy_backend.user.constant.UserRole.NORMAL_USER;

import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.auth.service.AuthService;
import com.example.kbuddy_backend.user.dto.request.LoginRequest;
import com.example.kbuddy_backend.user.dto.request.OAuthLoginRequest;
import com.example.kbuddy_backend.user.dto.request.OAuthRegisterRequest;
import com.example.kbuddy_backend.user.dto.request.PasswordRequest;
import com.example.kbuddy_backend.user.dto.request.RegisterRequest;
import com.example.kbuddy_backend.user.entity.Authority;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.exception.DuplicateUserException;
import com.example.kbuddy_backend.user.exception.InvalidPasswordException;
import com.example.kbuddy_backend.user.exception.UserNotFoundException;
import com.example.kbuddy_backend.user.repository.UserRepository;
import com.example.kbuddy_backend.user.util.UserConverter;
import com.example.kbuddy_backend.notification.service.FCMTokenService;
import com.example.kbuddy_backend.s3.service.S3Service;
import com.example.kbuddy_backend.user.repository.UserBlockRepository;
import com.example.kbuddy_backend.user.dto.AppleUserInfo;
import com.example.kbuddy_backend.user.dto.response.AppleLoginResponse;
import com.example.kbuddy_backend.user.constant.OAuthCategory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.kbuddy_backend.user.exception.AccountDeactivatedException;
import com.example.kbuddy_backend.user.exception.UserNotFoundException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Service
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final FCMTokenService fcmTokenService;
    private final S3Service s3Service;
    private final UserBlockRepository userBlockRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AccessTokenAndRefreshTokenResponse register(final RegisterRequest registerRequest) {

        //todo: final default 설정하기
        //todo: 유효성 검사 및 테스트 코드
        userRepository.findByUsernameOrEmailAndOauthCategoryIsNullAndOauthUidIsNull(registerRequest.userId(),
                registerRequest.email()).ifPresent(user -> {
            throw new DuplicateUserException();
        });
        final String password = passwordEncoder.encode(registerRequest.password());
        final User newUser = UserConverter.fromRegisterRequest(registerRequest, password);

        newUser.addAuthority(new Authority(NORMAL_USER));
        User saveUser = userRepository.save(newUser);
        UsernamePasswordAuthenticationToken authenticationToken = getUsernamePasswordAuthenticationToken(
                saveUser, password);

        return authService.createToken(authenticationToken);
    }

    @Transactional
    public AccessTokenAndRefreshTokenResponse oAuthRegister(final OAuthRegisterRequest registerRequest) {
        userRepository.findByOauthUidAndOauthCategory(registerRequest.oAuthUid(),
                registerRequest.oAuthCategory()).ifPresent(user -> {
            throw new DuplicateUserException();
        });

        final User newUser = UserConverter.fromOAuthRegisterRequest(registerRequest);

        newUser.addAuthority(new Authority(NORMAL_USER));
        User saveUser = userRepository.save(newUser);

        UsernamePasswordAuthenticationToken authenticationToken = getUsernamePasswordAuthenticationToken(
                saveUser, "oAuth");

        return authService.createToken(authenticationToken);
    }

    public boolean checkOAuthUser(final OAuthLoginRequest request) {
        return userRepository.findByOauthUidAndOauthCategory(request.oAuthUid(), request.oAuthCategory()).isPresent();
    }

    public AccessTokenAndRefreshTokenResponse login(final LoginRequest loginRequest) {

        final String emailOrUserId = loginRequest.emailOrUserId();
        final String password = loginRequest.password();

        //사용자 아이디 or 이메일을 통해 로그인
        User user = userRepository.findByUsernameOrEmailAndOauthCategoryIsNullAndOauthUidIsNull(emailOrUserId, emailOrUserId).orElseThrow(UserNotFoundException::new);

        if (!user.isActive()) {
            throw new AccountDeactivatedException();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException();
        }

        UsernamePasswordAuthenticationToken authenticationToken = getUsernamePasswordAuthenticationToken(
                user, password);

        return authService.createToken(authenticationToken);
    }

    public AccessTokenAndRefreshTokenResponse oAuthLogin(final OAuthLoginRequest loginRequest) {
        final User user = userRepository.findByOauthUidAndOauthCategory(loginRequest.oAuthUid(), loginRequest.oAuthCategory()).orElseThrow(UserNotFoundException::new);

        if (!user.isActive()) {
            throw new AccountDeactivatedException();
        }

        UsernamePasswordAuthenticationToken authenticationToken = getUsernamePasswordAuthenticationToken(
                user, "oAuth");

        return authService.createToken(authenticationToken);
    }

    @Transactional
    public void resetPassword(PasswordRequest passwordRequest, User user) {
        user.resetPassword(passwordEncoder.encode(passwordRequest.password()));
    }

    @Transactional
    public void deleteAccount(User user) {
        // 1. S3에 업로드된 프로필 이미지 삭제
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            try {
                s3Service.deleteFile(user.getProfileImageUrl());
            } catch (Exception e) {
                // S3 삭제 실패 시에도 계정 삭제는 진행
                // TODO: 로그 기록
            }
        }
        
        // 2. FCM 토큰 비활성화
        fcmTokenService.deactivateAllUserTokens(user);
        
        // 3. 차단 관계 삭제 (사용자가 차단한 사용자들, 사용자를 차단한 사용자들)
        userBlockRepository.deleteByBlocker(user);
        userBlockRepository.deleteByBlocked(user);
        
        // 4. 사용자 계정을 비활성화 처리
        user.deactivateAccount();
        
        // TODO: 추가 데이터 정리 로직
        // - 사용자가 작성한 게시글, 댓글, 좋아요, 북마크 등
        // - 알림 데이터 삭제
        // - Redis에 저장된 토큰 정보 삭제
    }

    @Transactional
    public AppleLoginResponse handleAppleLogin(String idToken, String userJson) {
        // 1. Apple ID Token 디코딩
        Map<String, Object> claims = decodeIdToken(idToken);
        String email = (String) claims.get("email");
        String sub = (String) claims.get("sub"); // Apple의 고유 ID
        
        // 2. user JSON 파싱 (이름 정보)
        AppleUserInfo userInfo = parseUserInfo(userJson);
        
        // 3. 기존 사용자 조회
        User user = userRepository.findByOauthUidAndOauthCategory(sub, OAuthCategory.APPLE)
                .orElse(null);
        
        boolean isNew = false;
        
        if (user == null) {
            isNew = true;
            return new AppleLoginResponse(
                    "",
                    "",
                    email,
                    sub,
                    userInfo != null ? userInfo.name().firstName() : "",
                    userInfo != null ? userInfo.name().lastName() : "",
                    isNew
            );
        }
        // 6. JWT 토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken = getUsernamePasswordAuthenticationToken(user, "apple");
        AccessTokenAndRefreshTokenResponse tokenResponse = authService.createToken(authenticationToken);
        
        return new AppleLoginResponse(
                tokenResponse.accessToken(),
                tokenResponse.refreshToken(),
                email,
                sub,
                user.getFirstName(),
                user.getLastName(),
                isNew
        );
    }

    private Map<String, Object> decodeIdToken(String idToken) {
        try {
            // JWT 토큰의 payload 부분 디코딩
            String[] tokenParts = idToken.split("\\.");
            String payload = tokenParts[1];
            
            // Base64 디코딩
            String decodedPayload = new String(java.util.Base64.getUrlDecoder().decode(payload));
            
            // JSON 파싱
            return objectMapper.readValue(decodedPayload, Map.class);
            
        } catch (Exception e) {
            throw new RuntimeException("Invalid Apple ID token", e);
        }
    }

    private AppleUserInfo parseUserInfo(String userJson) {
        try {
            if (userJson == null || userJson.isEmpty()) {
                return null;
            }
            return objectMapper.readValue(userJson, AppleUserInfo.class);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    private UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(User saveUser,
                                                                                              String password) {
        List<GrantedAuthority> grantedAuthorities = saveUser.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName().name()))
                .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(saveUser.getId(), password, grantedAuthorities);
    }

    // //Redis에 저장된 RefreshToken 삭제로직 필요
    // public void logout(User user) {
    //
    //     //쿠키에 저장된 refresh token 만료
    //
    // }
}
