package com.souma1024.shogiv2.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.souma1024.shogiv2.repository.RoomRepository;
import com.souma1024.shogiv2.websocket.ShogiWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RoomRepository roomRepository;

    WebSocketConfig(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }
    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(new ShogiWebSocketHandler(roomRepository), "/ws/shogi")
                .setAllowedOrigins("*");
    }
}
