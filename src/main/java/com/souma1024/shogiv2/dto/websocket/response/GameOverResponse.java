package com.souma1024.shogiv2.dto.websocket.response;

import com.souma1024.shogiv2.enums.game.GameOverReason;

import lombok.Data;

@Data
public class GameOverResponse {
    private String roomId;
    private String playerId;
    private String winner;
    private GameOverReason reason;
}
