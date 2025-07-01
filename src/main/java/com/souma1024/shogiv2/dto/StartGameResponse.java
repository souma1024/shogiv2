package com.souma1024.shogiv2.dto;

import lombok.Data;

@Data
public class StartGameResponse {
    private String roomId;
    private String status; // "waiting_for_opponent" または "started"
    private String playerId;

    public StartGameResponse() {
        // デフォルトコンストラクタ
    }

    public StartGameResponse(String roomId, String status) {
        this.roomId = roomId;
        this.status = status;
    }


}
