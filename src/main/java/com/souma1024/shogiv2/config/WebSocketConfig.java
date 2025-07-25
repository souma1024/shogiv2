package com.souma1024.shogiv2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.souma1024.shogiv2.repository.RoomRepository;
import com.souma1024.shogiv2.service.GameStartService;
import com.souma1024.shogiv2.service.MovablePositionService;
import com.souma1024.shogiv2.service.MoveService;
import com.souma1024.shogiv2.service.RoomSessionManager;
import com.souma1024.shogiv2.websocket.ShogiWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameStartService gameStartService;
    private final RoomRepository roomRepository;
    private final RoomSessionManager roomManager;
    private final MovablePositionService movablePositionService;
    private final MoveService moveService;


    WebSocketConfig(RoomRepository roomRepository, GameStartService gameStartService, RoomSessionManager roomManager, MovablePositionService movablePositionService, MoveService moveService) {
        this.roomRepository = roomRepository;
        this.gameStartService = gameStartService; 
        this.roomManager = roomManager;
        this.movablePositionService = movablePositionService;
        this.moveService = moveService;
    }
    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(new ShogiWebSocketHandler(roomRepository, gameStartService, roomManager, movablePositionService, moveService), "/ws/shogi")
                .setAllowedOrigins("*");
    }
}
