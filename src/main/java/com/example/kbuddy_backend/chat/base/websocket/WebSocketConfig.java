package com.example.kbuddy_backend.chat.base.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

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
                .withSockJS();
    }
}
