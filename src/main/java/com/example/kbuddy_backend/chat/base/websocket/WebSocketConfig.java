package com.example.kbuddy_backend.chat.base.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import com.example.kbuddy_backend.auth.token.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.core.Authentication;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커 설정
        // /sub 구독 주소 prefix
        // /pub: 메시지 전송 주소 prefix
        registry.enableSimpleBroker("/sub"); // "/sub"이 붙은 메시지를 발행할 경우, 메시지 브로커가 이를 처리
        registry.setApplicationDestinationPrefixes("/pub"); // 메세지 핸들러로 라우팅도는 "/pub"을 파라미터로 지정할 수 있다.
        // 메시지 가공 처리가 필요할 경우, 가공 핸들러로 메시지를 라우팅 되도록하는 설정
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 웹소켓 엔드포인트 설정
        // SockJS를 통한 웹소캣 연결 지원
        // stomp 접속 주소 url = ws://localhost:8080/ws, 프로토콜이 http가 아님
        registry.addEndpoint("/ws-stomp") // 처음 웹소켓 HandShake를 위한 접속 경로
            .setAllowedOriginPatterns("*");
        // .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String jwt = accessor.getFirstNativeHeader("Authorization");
                    if (jwt != null && jwt.startsWith("Bearer ")) {
                        jwt = jwt.substring(7);
                        if (jwtTokenProvider.validateToken(jwt)) {
                            Authentication auth = jwtTokenProvider.getAuthentication(jwt);
                            accessor.setUser(auth);
                        } else {
                            // 토큰이 유효하지 않은 경우 연결 거부
                            throw new IllegalArgumentException("Invalid JWT token");
                        }
                    } else {
                        throw new IllegalArgumentException("Authorization header missing or invalid");
                    }
                }
                return message;
            }
        });
    }
}
