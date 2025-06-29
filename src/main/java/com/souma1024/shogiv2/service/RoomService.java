package com.souma1024.shogiv2.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.souma1024.shogiv2.common.enums.RoomStatus;
import com.souma1024.shogiv2.model.Room;
import com.souma1024.shogiv2.repository.RoomRepository;

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

        Room room = new Room(
            roomId,
            firstPlayerId,
            null, // プレイヤー2はまだ参加していない
            status,
            timeLimit,
            LocalDateTime.now()
        );
        return roomRepository.save(room);
    }

    public String generateRoomId() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    public String generatePlayerId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
