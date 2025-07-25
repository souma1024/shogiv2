package com.souma1024.shogiv2.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.souma1024.shogiv2.dto.room.JoinRoomResponse;
import com.souma1024.shogiv2.entity.Room;
import com.souma1024.shogiv2.enums.common.PlayerStatus;
import com.souma1024.shogiv2.enums.common.RoomStatus;
import com.souma1024.shogiv2.repository.RoomRepository;

import jakarta.transaction.Transactional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room createRoom(Integer timeLimit) {
        String roomId = generateRoomId();
        String firstPlayerId = generatePlayerId();
        RoomStatus status = RoomStatus.CREATED;
        PlayerStatus playerStatus = PlayerStatus.NOT_READY;

        Room room = new Room(
            roomId,
            firstPlayerId,
            null, // プレイヤー2はまだ参加していない
            playerStatus,
            null,
            status,
            timeLimit,
            LocalDateTime.now()
        );
        return roomRepository.save(room);
    }

    @Transactional
    public JoinRoomResponse joinRoom(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("ルームが見つかりません"));

        if (room.getSecondPlayerId() != null) {
            throw new IllegalStateException("このルームにはすでに2人参加しています");
        }

        String secondPlayerId = generatePlayerId(); // ランダム生成メソッド
        room.setSecondPlayerId(secondPlayerId);
        room.setStatus(RoomStatus.WAITING);
        room.setSecondPlayerStatus(PlayerStatus.NOT_READY);
        roomRepository.save(room);

        return new JoinRoomResponse(
                room.getRoomId(),
                secondPlayerId,
                room.getTimeLimit(),
                room.getStatus(),
                "ルームに参加できました"
        );
    }

    public String generateRoomId() {
        return UUID.randomUUID().toString().substring(0, 7);
    }

    public String generatePlayerId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
