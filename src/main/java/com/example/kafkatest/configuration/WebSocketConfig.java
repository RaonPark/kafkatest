package com.example.kafkatest.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 소켓의 엔드포인트를 설정한다. 이 주소로 소켓이 handshake된다.
        registry.addEndpoint("/kafkatest");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // brokder를 등록한다. 브로커의 주소는 /topic이며, 브로커가 이 주소로 메세지를 처리한다.
        // /topic이라 이름붙은 브로커가 메세지를 들고다니며, /topic이라 이름붙은 클라이언트에 메세지를 보낸다.
        registry.enableSimpleBroker("/topic");
        // 모든 /app/* 에 메세지를 매핑한다. @MessageMapping은 이제 prefix로 app을 갖는다.
        registry.setApplicationDestinationPrefixes("/app");
    }
}
