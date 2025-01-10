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
        final String username = registerRequest.userId();

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            throw new DuplicateUserException();
        }

        final String password = passwordEncoder.encode(registerRequest.password());

        final User newUser = User.builder()
                .username(username)
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .country(registerRequest.country())
                .gender(registerRequest.gender())
                .email(email)
                .password(password).build();

        newUser.addAuthority(new Authority(NORMAL_USER));
        User saveUser = userRepository.save(newUser);

        List<GrantedAuthority> grantedAuthorities = saveUser.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName().name()))
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password, grantedAuthorities);

        return authService.createToken(authenticationToken);
    }

    @Transactional
    public AccessTokenAndRefreshTokenResponse oAuthRegister(final OAuthRegisterRequest registerRequest) {

        final String email = registerRequest.email();
        final String username = registerRequest.userId();

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            throw new DuplicateUserException();
        }

        final User newUser = User.builder()
                .username(username)
                .oAuthCategory(registerRequest.oauth())
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .country(registerRequest.country())
                .gender(registerRequest.gender())
                .email(email).build();

        newUser.addAuthority(new Authority(NORMAL_USER));
        User saveUser = userRepository.save(newUser);

        List<GrantedAuthority> grantedAuthorities = saveUser.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName().name()))
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, "oAuth", grantedAuthorities);

        return authService.createToken(authenticationToken);
    }

    public boolean checkOAuthUser(final OAuthLoginRequest request) {
        return userRepository.findByEmailAndOauthCategory(request.email(), request.oauth()).isPresent();
    }

    public AccessTokenAndRefreshTokenResponse login(final LoginRequest loginRequest) {

        final String email = loginRequest.email();
        final String password = loginRequest.password();

        final Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        final User findUser = user.get();

        if (!passwordEncoder.matches(password, findUser.getPassword())) {
            throw new InvalidPasswordException();
        }
        List<GrantedAuthority> grantedAuthorities = findUser.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName().name()))
                .collect(Collectors.toList());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password, grantedAuthorities);

        return authService.createToken(authenticationToken);
    }

    public AccessTokenAndRefreshTokenResponse oAuthLogin(final OAuthLoginRequest loginRequest) {

        final String email = loginRequest.email();

        final Optional<User> user = userRepository.findByEmailAndOauthCategory(email, loginRequest.oauth());

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        final User findUser = user.get();

        List<GrantedAuthority> grantedAuthorities = findUser.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName().name()))
                .collect(Collectors.toList());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, "oAuth", grantedAuthorities);

        return authService.createToken(authenticationToken);
    }

    @Transactional
    public void resetPassword(PasswordRequest passwordRequest, User user) {
        user.resetPassword(passwordEncoder.encode(passwordRequest.password()));
    }
}
