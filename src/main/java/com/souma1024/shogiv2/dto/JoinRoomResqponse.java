package com.souma1024.shogiv2.dto;

import com.souma1024.shogiv2.common.enums.RoomStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinRoomResqponse {
    private String roomId;
    private String playerId;
    private int timeLimit;
    private RoomStatus status;
    private String message;
}
