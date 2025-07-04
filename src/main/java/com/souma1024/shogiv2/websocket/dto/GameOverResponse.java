package com.souma1024.shogiv2.websocket.dto;

import com.souma1024.shogiv2.websocket.dto.enums.GameOverReason;

import lombok.Data;

@Data
public class GameOverResponse {
    private String roomId;
    private String playerId;
    private String winner;
    private GameOverReason reason;
}
