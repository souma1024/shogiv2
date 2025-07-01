package com.souma1024.shogiv2.websocket.dto;

import com.souma1024.shogiv2.websocket.dto.enums.WebSocketType;

import lombok.Data;

@Data
public class MoveResponse {
    private final WebSocketType type = WebSocketType.MOVE_RESPONSE;
    private String roomId;
    private String playerId;
    private int[] from;
    private int[] to;
    private int kind;
    private boolean promotion;
    private boolean isSuccess;
    private String nextPlayerId;
}
