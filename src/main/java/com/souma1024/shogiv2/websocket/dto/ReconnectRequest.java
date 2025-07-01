package com.souma1024.shogiv2.websocket.dto;

import com.souma1024.shogiv2.websocket.dto.enums.WebSocketType;

import lombok.Data;

@Data
public class ReconnectRequest {
    private final WebSocketType type = WebSocketType.RECONNECT_REQUEST;
    private String roomId;
    private String playerId;
}
