package com.souma1024.shogiv2.websocket.dto;

import com.souma1024.shogiv2.websocket.dto.enums.GameOverReason;
import com.souma1024.shogiv2.websocket.dto.enums.WebSocketType;

import lombok.Data;

@Data
public class GameOverRequest {
    private final WebSocketType type = WebSocketType.GAME_OVER_REQUEST;
    private String roomId;
    private String playerId;
    private GameOverReason reason;

}
