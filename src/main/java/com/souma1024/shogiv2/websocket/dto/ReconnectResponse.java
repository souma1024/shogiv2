package com.souma1024.shogiv2.websocket.dto;


import lombok.Data;

@Data
public class ReconnectResponse {
    private String roomId;
    private String currentState;
    private boolean isSuccess;
    private String message;
}
