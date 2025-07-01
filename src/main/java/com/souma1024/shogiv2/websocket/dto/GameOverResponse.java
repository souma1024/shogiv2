package com.souma1024.shogiv2.websocket.dto;

import com.souma1024.shogiv2.websocket.dto.enums.GameOverReason;
import com.souma1024.shogiv2.websocket.dto.enums.WebSocketType;

import lombok.Data;

@Data
public class GameOverResponse {
    private final WebSocketType type = WebSocketType.GAME_OVER_RESPONSE;
    private String roomId;
    private String playerId;
    private String winner;
    private GameOverReason reason;
}
