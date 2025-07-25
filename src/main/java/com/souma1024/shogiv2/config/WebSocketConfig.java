package com.souma1024.shogiv2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.souma1024.shogiv2.service.GameOverService;
import com.souma1024.shogiv2.service.GameStartService;
import com.souma1024.shogiv2.service.MovablePositionService;
import com.souma1024.shogiv2.service.MoveService;
import com.souma1024.shogiv2.service.ReconnectService;
import com.souma1024.shogiv2.service.RoomSessionManager;
import com.souma1024.shogiv2.websocket.ShogiWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameStartService gameStartService;
    private final RoomSessionManager roomManager;
    private final MovablePositionService movablePositionService;
    private final MoveService moveService;
    private final GameOverService gameOverService;
    private final ReconnectService reconnectService;


    WebSocketConfig(
     GameStartService gameStartService, 
     RoomSessionManager roomManager, 
     MovablePositionService movablePositionService, 
     MoveService moveService,
     GameOverService gameOverService,
     ReconnectService reconnectService)
      {
        this.gameStartService = gameStartService; 
        this.roomManager = roomManager;
        this.movablePositionService = movablePositionService;
        this.moveService = moveService;
        this.gameOverService = gameOverService;
        this.reconnectService = reconnectService;
    }
    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(new ShogiWebSocketHandler(gameStartService, roomManager, movablePositionService, moveService, gameOverService, reconnectService), "/ws/shogi")
                .setAllowedOrigins("*");
    }
}
