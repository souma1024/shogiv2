package com.souma1024.shogiv2.websocket.dto;

import lombok.Data;

@Data
public class MovablePositionResponse {
    private String roomId;
    private String playerId;
    private int[] from;
    private int kind;
    private int[][] movable;
}
