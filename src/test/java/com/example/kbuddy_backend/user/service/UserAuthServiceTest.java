package com.example.kbuddy_backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.common.IntegrationTest;
import com.example.kbuddy_backend.common.config.DataInitializer;
import com.example.kbuddy_backend.fixtures.UserFixtures;
import com.example.kbuddy_backend.user.constant.Country;
import com.example.kbuddy_backend.user.constant.Gender;
import com.example.kbuddy_backend.user.constant.OAuthCategory;
import com.example.kbuddy_backend.user.dto.request.LoginRequest;
import com.example.kbuddy_backend.user.dto.request.OAuthLoginRequest;
import com.example.kbuddy_backend.user.dto.request.PasswordRequest;
import com.example.kbuddy_backend.user.dto.request.RegisterRequest;
import com.example.kbuddy_backend.user.entity.User;
import com.example.kbuddy_backend.user.exception.DuplicateUserException;
import com.example.kbuddy_backend.user.exception.InvalidPasswordException;
import com.example.kbuddy_backend.user.exception.UserNotFoundException;
import com.example.kbuddy_backend.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

class UserAuthServiceTest extends IntegrationTest {

    @Autowired
    private UserAuthService userAuthService;

    @MockBean
    private DataInitializer dataInitializer;

    @MockBean
    private UserRepository userRepository;
    @Mock
    private RegisterRequest mockRegisterRequest;

    @DisplayName("회원가입 성공시 토큰을 반환한다.")
    @Test
    void loginSuccess() {
        //given
        RegisterRequest registerRequest = RegisterRequest.of("test", "test", "test", "test", "test", Country.KOREA,
            Gender.M);
        given(userRepository.save(any(User.class))).willReturn(UserFixtures.createUser());

        //when
        AccessTokenAndRefreshTokenResponse register = userAuthService.register(registerRequest);

        //then
        assertThat(register).extracting("accessToken").isNotNull();
        assertThat(register).extracting("refreshToken").isNotNull();
    }

    @DisplayName("패스워드가 일치하지 않으면 에러를 던진다.")
    @Test
    void checkInvalidPassword() {
        //given
        User user = UserFixtures.createUser();
        LoginRequest loginRequest = LoginRequest.of("test", "test");
        given(mockRegisterRequest.password()).willReturn("invalidPassword");
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        //then
        assertThatThrownBy(() -> userAuthService.login(loginRequest))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("비밀번호가 일치하지 않습니다.");
    }

    @DisplayName("아이디가 일치하지 않으면 에러를 던진다.")
    @Test
    void checkNotFoundUser() {
        //given
        LoginRequest loginRequest = LoginRequest.of("test", "test");
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> userAuthService.login(loginRequest))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @DisplayName("중복된 이메일을 확인한다.")
    @Test
    void checkDuplicatedEmail() {
        //given
        User user = UserFixtures.createUser();
        RegisterRequest registerRequest = RegisterRequest.of("test", "test", "test", "test", "test", Country.KOREA,
            Gender.M);
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        //then
        assertThatThrownBy(() -> userAuthService.register(registerRequest))
            .isInstanceOf(DuplicateUserException.class)
            .hasMessageContaining("이미 존재하는 사용자입니다.");
    }

    @DisplayName("OAuth로 가입된 회원인지 확인한다.")
    @Test
    void checkOAuthRegisterUser() {
        //given
        OAuthLoginRequest oAuthLoginRequest = OAuthLoginRequest.of("k-buddy@gmail.com", OAuthCategory.KAKAO);
        given(userRepository.findByEmailAndOauthCategory(anyString(), any())).willReturn(
            Optional.of(UserFixtures.createOAuthUser()));

        //then
        assertThat(userAuthService.checkOAuthUser(oAuthLoginRequest)).isTrue();
    }

    @DisplayName("비밀번호가 정상적으로 변경되는지 확인한다.")
    @Test
    void checkChangePassword() {
        //given
        User user = UserFixtures.createUser();
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        //when
        userAuthService.resetPassword(PasswordRequest.of("changePassword"), user);
        AccessTokenAndRefreshTokenResponse changePasswordUser = userAuthService.login(
            LoginRequest.of(user.getEmail(), "changePassword"));
        //then
        assertThat(changePasswordUser).extracting("accessToken").isNotNull();
        assertThat(changePasswordUser).extracting("refreshToken").isNotNull();
    }
}