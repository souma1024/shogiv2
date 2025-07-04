package com.souma1024.shogiv2.websocket.dto;

import com.souma1024.shogiv2.websocket.dto.enums.GameOverReason;

import lombok.Data;

@Data
public class GameOverRequest {
    private String roomId;
    private String playerId;
    private GameOverReason reason;

}
