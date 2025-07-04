package com.souma1024.shogiv2.websocket.dto;


import lombok.Data;

@Data
public class ReconnectRequest {
    private String roomId;
    private String playerId;
}
