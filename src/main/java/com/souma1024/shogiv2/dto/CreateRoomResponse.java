package com.souma1024.shogiv2.dto;

public class CreateRoomResponse {
     private String roomId;
    private String playerId;
    private int timeLimit;
    private String status;

    public CreateRoomResponse(String roomId, String playerId, int timeLimit, String status) {
        this.roomId = roomId;
        this.playerId = playerId;
        this.timeLimit = timeLimit;
        this.status = status;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public String getStatus() {
        return status;
    }
}
