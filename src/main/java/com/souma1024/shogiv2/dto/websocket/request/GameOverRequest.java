package com.souma1024.shogiv2.dto.websocket.request;

import com.souma1024.shogiv2.enums.game.GameOverReason;

import lombok.Data;

@Data
public class GameOverRequest {
    private String roomId;
    private String playerId;
    private GameOverReason reason;

}
