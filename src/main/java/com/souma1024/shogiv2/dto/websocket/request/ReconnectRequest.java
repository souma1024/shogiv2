package com.souma1024.shogiv2.dto.websocket.request;


import lombok.Data;

@Data
public class ReconnectRequest {
    private String roomId;
    private String playerId;
}
