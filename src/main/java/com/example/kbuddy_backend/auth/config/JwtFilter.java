package com.example.kbuddy_backend.auth.config;

import com.example.kbuddy_backend.auth.token.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 무효한 토큰이 실려와도 거부하지 않고 통과시키는 경로.
     *
     * 클라이언트가 액세스 토큰을 axios 전역 헤더에 보관하기 때문에, 토큰이 만료된 뒤에도
     * 로그인/재발급 요청에 만료 토큰이 그대로 실려온다. 이 경로들을 거부하면 만료 이후
     * 재발급도 재로그인도 불가능해지므로 반드시 예외로 둔다.
     *
     * 인증 없이 접근 가능한(permitAll) 경로를 새로 추가할 때는,
     * 그 경로가 만료 토큰을 실어 보낼 수 있는지 반드시 함께 검토해야 한다.
     */
    private static final List<String> TOKEN_VALIDATION_EXCLUDED_PREFIXES = List.of(
            "/kbuddy/v1/auth/",
            "/kbuddy/v1/admin/login",
            "/kbuddy/v1/admin/refresh",
            "/ws-stomp"
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    //토큰의 인증정보를 SecurityContext에 저장하는 역할
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final String token = resolveToken(httpServletRequest);
        final String requestURI = httpServletRequest.getRequestURI();

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(authentication);
            log.info("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
            chain.doFilter(request, response);
            return;
        }

        //토큰을 제시했는데 무효한 경우, 익명으로 강등하지 않고 401을 반환한다.
        //그렇지 않으면 게스트 조회가 허용된 경로에서 만료 토큰이 조용히 익명 응답으로 처리되어
        //클라이언트의 토큰 재발급 트리거가 사라진다.
        if (token != null && !isTokenValidationExcluded(requestURI)) {
            log.info("유효하지 않은 JWT 토큰입니다, uri: {}", requestURI);
            SecurityContextHolder.clearContext();
            jwtAuthenticationEntryPoint.commence(httpServletRequest, (HttpServletResponse) response,
                    new InsufficientAuthenticationException("유효하지 않은 토큰입니다."));
            return;
        }

        log.info("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        chain.doFilter(request, response);
    }

    private boolean isTokenValidationExcluded(String requestURI) {
        return TOKEN_VALIDATION_EXCLUDED_PREFIXES.stream().anyMatch(requestURI::startsWith);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
