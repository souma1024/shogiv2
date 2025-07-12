package com.souma1024.shogiv2.dto.websocket.response;


import com.souma1024.shogiv2.dto.websocket.common.CapturedPiece;

import lombok.Data;

@Data
public class MoveResponse {
    private String roomId;
    private String playerId;
    private int[] from;
    private int[] to;
    private int piece;
    private boolean promotion;
    private boolean isSuccess;
    private String nextPlayerId;
    private CapturedPiece captured; // nullable
}
