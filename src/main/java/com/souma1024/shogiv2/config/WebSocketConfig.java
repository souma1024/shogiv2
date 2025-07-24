package com.souma1024.shogiv2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.souma1024.shogiv2.repository.RoomRepository;
import com.souma1024.shogiv2.service.GameStartService;
import com.souma1024.shogiv2.service.RoomSessionManager;
import com.souma1024.shogiv2.websocket.ShogiWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameStartService gameStartService;
    private final RoomRepository roomRepository;
    private final RoomSessionManager roomManager;

    WebSocketConfig(RoomRepository roomRepository, GameStartService gameStartService, RoomSessionManager roomManager) {
        this.roomRepository = roomRepository;
        this.gameStartService = gameStartService; 
        this.roomManager = roomManager;
    }
    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(new ShogiWebSocketHandler(roomRepository, gameStartService, roomManager), "/ws/shogi")
                .setAllowedOrigins("*");
    }
}
