package com.souma1024.shogiv2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.souma1024.shogiv2.dto.CreateRoomRequest;
import com.souma1024.shogiv2.dto.CreateRoomResponse;
import com.souma1024.shogiv2.dto.ErrorResponse;
import com.souma1024.shogiv2.dto.JoinRoomResqponse;
import com.souma1024.shogiv2.model.Room;
import com.souma1024.shogiv2.repository.RoomRepository;
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
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId) {
        try {
            JoinRoomResqponse response = roomService.joinRoom(roomId);
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
