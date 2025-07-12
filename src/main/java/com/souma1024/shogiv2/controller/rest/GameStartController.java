package com.souma1024.shogiv2.controller.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.souma1024.shogiv2.entity.Room;
import com.souma1024.shogiv2.enums.common.PlayerStatus;
import com.souma1024.shogiv2.enums.common.RoomStatus;
import com.souma1024.shogiv2.repository.RoomRepository;


@RestController
@RequestMapping("/api/rooms")
public class GameStartController {


    private final RoomRepository roomRepository;

    public GameStartController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    // プレイヤーが対局開始ボタンを押した時のエンドポイント
    @PostMapping("/{roomId}/start")
    public ResponseEntity<?> startGame(@PathVariable String roomId, @RequestBody Map<String, String> body) throws Exception {
        String playerId = body.get("playerId");

        Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (playerId.equals(room.getFirstPlayerId())) {
                room.setFirstPlayerStatus(PlayerStatus.READY);
            } else if (playerId.equals(room.getSecondPlayerId())) {
                room.setSecondPlayerStatus(PlayerStatus.READY);
            } else {
                return ResponseEntity.badRequest().body("Invalid playerId for this room");
            }

            // 両者READYなら roomStatus を READY に
            if (room.getFirstPlayerStatus() == PlayerStatus.READY &&
                room.getSecondPlayerStatus() == PlayerStatus.READY) {
                room.setStatus(RoomStatus.READY);
            }

            roomRepository.save(room);

            return ResponseEntity.ok(Map.of(
                "roomId", roomId,
                "playerId", playerId,
                "status", room.getStatus()
            ));
        
    }
}
