package com.souma1024.shogiv2.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.souma1024.shogiv2.common.errors.ErrorResponse;
import com.souma1024.shogiv2.domain.model.Player;
import com.souma1024.shogiv2.dto.room.CreateRoomRequest;
import com.souma1024.shogiv2.dto.room.CreateRoomResponse;
import com.souma1024.shogiv2.dto.room.JoinRoomResponse;
import com.souma1024.shogiv2.entity.Room;
import com.souma1024.shogiv2.enums.common.PlayerSide;
import com.souma1024.shogiv2.repository.RoomRepository;
import com.souma1024.shogiv2.service.RoomSessionManager;
import com.souma1024.shogiv2.service.RoomService;

@RestController
@RequestMapping("/api")
public class RoomController {
    
    private final RoomService roomService;

    public RoomController(RoomService roomService, RoomRepository roomRepository) {
        this.roomService = roomService;
    }

    @PostMapping("/rooms") 
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest request) {
        int timeLimit = request.getTimeLimit();
        if (timeLimit <= 0 || timeLimit > 360) {
            ErrorResponse error = new ErrorResponse(
                400,
                "Bad Request",
                "持ち時間が不正です",
                "/api/rooms"
                );
            return ResponseEntity.badRequest().body(error);
        }

        Room room = roomService.createRoom(timeLimit);

        CreateRoomResponse response = new CreateRoomResponse(room.getRoomId(), room.getFirstPlayerId(), timeLimit, room.getStatus(), "ルーム作成に成功しました");
        RoomSessionManager.getInstance().canAddPlayer(response.getRoomId(), new Player(response.getPlayerId(), PlayerSide.SENTE));
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId) {
        try {
            JoinRoomResponse response = roomService.joinRoom(roomId);
            Player player = new Player(response.getPlayerId(), PlayerSide.GOTE); // or SENTE
            RoomSessionManager.getInstance().canAddPlayer(roomId, player);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(
                400,
                "Not Found",
                e.getMessage(),
                "/api/rooms/" + roomId + "/join"
            );
            return ResponseEntity.badRequest().body(error);
        } catch (IllegalStateException e) {
            ErrorResponse error = new ErrorResponse(
                404,
                "Bad Request",
                e.getMessage(),
                "/api/rooms/" + roomId + "/join"
            );
            return ResponseEntity.badRequest().body(error);
        }
    }
}
