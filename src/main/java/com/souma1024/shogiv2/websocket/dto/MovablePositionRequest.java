package com.souma1024.shogiv2.websocket.dto;

import lombok.Data;

@Data
public class MovablePositionRequest {
    private String roomId;
    private String playerId;
    private int[] from;
    private int piece;
    private boolean promotion;
}
