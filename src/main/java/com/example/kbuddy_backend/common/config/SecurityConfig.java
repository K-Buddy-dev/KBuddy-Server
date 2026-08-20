package com.example.kbuddy_backend.common.config;

import com.example.kbuddy_backend.auth.config.JwtAccessDeniedHandler;
import com.example.kbuddy_backend.auth.config.JwtAuthenticationEntryPoint;
import com.example.kbuddy_backend.auth.config.JwtFilter;
import com.example.kbuddy_backend.auth.token.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtTokenProvider tokenProvider;

    //시큐리티를 적용하지 않을 리소스
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/error", "favicon.ico", "/h2-console/**", "/api-docs/**", "/swagger-ui/**", "/v3/api-docs/**", "/index.html");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors->cors.configure(http))
                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .addFilterBefore(new JwtFilter(tokenProvider, jwtAuthenticationEntryPoint), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> {
                    exceptionHandling
                            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                            .accessDeniedHandler(jwtAccessDeniedHandler);
                })
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/kbuddy/v1/admin/login", "/kbuddy/v1/admin/refresh").permitAll()
                                .requestMatchers("/kbuddy/v1/admin/**").hasRole("ADMIN")
                                .requestMatchers("/kbuddy/v1/auth/password","/kbuddy/v1/auth/authentication","/kbuddy/v1/auth/account").authenticated()
                                .requestMatchers("/kbuddy/v1/auth/**","/actuator/health","/ws-stomp/**","/ws-stomp").permitAll()
                                //본인 전용 조회는 아래 게스트 허용 매처보다 먼저 선언해야 한다.
                                .requestMatchers(HttpMethod.GET, "/kbuddy/v1/counselor/me").authenticated()
                                //비로그인 사용자도 둘러볼 수 있는 조회 API (GET 한정)
                                .requestMatchers(HttpMethod.GET,
                                        "/kbuddy/v1/blog",
                                        "/kbuddy/v1/blog/*",
                                        "/kbuddy/v1/qna",
                                        "/kbuddy/v1/qna/*",
                                        "/kbuddy/v1/counselor",
                                        "/kbuddy/v1/counselor/*",
                                        "/kbuddy/v1/counselor/*/availability",
                                        "/kbuddy/v1/counselor/*/review",
                                        "/kbuddy/v1/counselor/*/inquiry",
                                        "/kbuddy/v1/counselor/*/inquiry/*").permitAll()
                                .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
