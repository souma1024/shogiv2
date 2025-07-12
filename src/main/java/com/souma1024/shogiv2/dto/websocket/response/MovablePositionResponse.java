package com.souma1024.shogiv2.dto.websocket.response;

import java.util.List;

import lombok.Data;

@Data
public class MovablePositionResponse {
    private String roomId;
    private String playerId;
    private int[] from;
    private int piece;
    private List<int[]> movable;
}
