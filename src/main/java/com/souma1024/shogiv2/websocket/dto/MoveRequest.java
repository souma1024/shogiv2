package com.souma1024.shogiv2.websocket.dto;


import lombok.Data;

@Data
public class MoveRequest {

    public MoveRequest() {}

    private String roomId;
    private String playerId;
    private int[] from;
    private int[] to;
    private int kind;
    private boolean promotion;
}
