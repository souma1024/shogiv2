package com.souma1024.shogiv2.dto.room;

import com.souma1024.shogiv2.enums.common.RoomStatus;

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
