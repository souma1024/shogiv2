package com.souma1024.shogiv2.dto.websocket.response;


import lombok.Data;

@Data
public class ReconnectResponse {
    private String roomId;
    private String currentState;
    private boolean isSuccess;
    private String message;
}
