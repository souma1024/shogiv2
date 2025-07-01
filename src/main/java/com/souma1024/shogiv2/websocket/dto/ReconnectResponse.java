package com.souma1024.shogiv2.websocket.dto;

import com.souma1024.shogiv2.websocket.dto.enums.WebSocketType;

import lombok.Data;

@Data
public class ReconnectResponse {
    private final WebSocketType type = WebSocketType.RECONNECT_RESPONSE;
    private String roomId;
    private String currentState;
    private boolean isSuccess;
}
