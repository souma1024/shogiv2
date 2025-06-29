package com.souma1024.shogiv2.dto;

import com.souma1024.shogiv2.common.enums.RoomStatus;

public class CreateRoomResponse {
    private String roomId;
    private String playerId;
    private int timeLimit;
    private RoomStatus status;

    public CreateRoomResponse(String roomId, String playerId, int timeLimit, RoomStatus status) {
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

    public RoomStatus getStatus() {
        return status;
    }
}
