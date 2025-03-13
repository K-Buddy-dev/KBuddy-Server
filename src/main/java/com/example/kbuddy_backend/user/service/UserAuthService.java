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
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

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

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException();
        }

        UsernamePasswordAuthenticationToken authenticationToken = getUsernamePasswordAuthenticationToken(
                user, password);

        return authService.createToken(authenticationToken);
    }

    public AccessTokenAndRefreshTokenResponse oAuthLogin(final OAuthLoginRequest loginRequest) {
        final User user = userRepository.findByOauthUidAndOauthCategory(loginRequest.oAuthUid(), loginRequest.oAuthCategory()).orElseThrow(UserNotFoundException::new);
        UsernamePasswordAuthenticationToken authenticationToken = getUsernamePasswordAuthenticationToken(
                user, "oAuth");

        return authService.createToken(authenticationToken);
    }

    @Transactional
    public void resetPassword(PasswordRequest passwordRequest, User user) {
        user.resetPassword(passwordEncoder.encode(passwordRequest.password()));
    }

    private UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(User saveUser,
                                                                                              String password) {
        List<GrantedAuthority> grantedAuthorities = saveUser.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName().name()))
                .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(saveUser.getId(), password, grantedAuthorities);
    }
}
