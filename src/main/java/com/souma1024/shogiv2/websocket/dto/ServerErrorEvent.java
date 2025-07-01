package com.souma1024.shogiv2.websocket.dto;

import com.souma1024.shogiv2.websocket.dto.enums.WebSocketType;

import lombok.Data;

@Data
public class ServerErrorEvent {
    private final WebSocketType type = WebSocketType.SERVER_ERROR_EVENT;
    private String roomId;
    private String reason; // e.g. connection_error
    private int code;
    private String message;

}
