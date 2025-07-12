package com.souma1024.shogiv2.entity;

import java.time.LocalDateTime;

import com.souma1024.shogiv2.enums.common.PlayerStatus;
import com.souma1024.shogiv2.enums.common.RoomStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Entity
@NoArgsConstructor
@Table(name = "room")
public class Room {

    @Id
    private String roomId;

    private String firstPlayerId;

    private String secondPlayerId;

    @Enumerated(EnumType.STRING)
    private PlayerStatus firstPlayerStatus;

    @Enumerated(EnumType.STRING)
    private PlayerStatus secondPlayerStatus;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    private Integer timeLimit;

    private LocalDateTime createdAt;
}
