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
import java.util.List;
import java.util.Optional;
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
        final String email = registerRequest.email();
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            throw new DuplicateUserException();
        }

        final String password = passwordEncoder.encode(registerRequest.password());
        final User newUser = createUser(registerRequest, password);

        newUser.addAuthority(new Authority(NORMAL_USER));
        User saveUser = userRepository.save(newUser);

        UsernamePasswordAuthenticationToken authenticationToken = getUsernamePasswordAuthenticationToken(
                saveUser, password);

        return authService.createToken(authenticationToken);
    }

    private static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(User saveUser,
                                                                                              String password) {
        List<GrantedAuthority> grantedAuthorities = saveUser.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName().name()))
                .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(saveUser.getId(), password, grantedAuthorities);
    }

    private static User createUser(RegisterRequest registerRequest,String password) {
        return User.builder()
                .username(registerRequest.userId())
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .country(registerRequest.country())
                .gender(registerRequest.gender())
                .birthDate(registerRequest.birthDate())
                .email(registerRequest.email())
                .password(password).build();
    }

    @Transactional
    public AccessTokenAndRefreshTokenResponse oAuthRegister(final OAuthRegisterRequest registerRequest) {
        Optional<User> user = userRepository.findByOauthUidAndOauthCategory(registerRequest.oAuthUid(),
                registerRequest.oAuthCategory());

        if (user.isPresent()) {
            throw new DuplicateUserException();
        }

        final User newUser = createOAuthUser(registerRequest);

        newUser.addAuthority(new Authority(NORMAL_USER));
        User saveUser = userRepository.save(newUser);

        UsernamePasswordAuthenticationToken authenticationToken = getUsernamePasswordAuthenticationToken(
                saveUser, "oAuth");

        return authService.createToken(authenticationToken);
    }

    private User createOAuthUser(OAuthRegisterRequest registerRequest) {
        return User.builder()
                .username(registerRequest.userId())
                .oAuthCategory(registerRequest.oAuthCategory())
                .oAuthUid(registerRequest.oAuthUid())
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .country(registerRequest.country())
                .gender(registerRequest.gender())
                .birthDate(registerRequest.birthDate())
                .email(registerRequest.email()).build();
    }

    public boolean checkOAuthUser(final OAuthLoginRequest request) {
        return userRepository.findByOauthUidAndOauthCategory(request.oAuthUid(), request.oAuthCategory()).isPresent();
    }

    public AccessTokenAndRefreshTokenResponse login(final LoginRequest loginRequest) {

        final String emailOrUserId = loginRequest.emailOrUserId();
        final String password = loginRequest.password();

        //사용자 아이디 or 이메일을 통해 로그인
        User user = userRepository.findByUsernameOrEmail(emailOrUserId, emailOrUserId).orElseThrow(UserNotFoundException::new);
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
}
