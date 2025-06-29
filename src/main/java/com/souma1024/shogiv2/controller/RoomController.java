package com.souma1024.shogiv2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.souma1024.shogiv2.dto.CreateRoomRequest;
import com.souma1024.shogiv2.dto.CreateRoomResponse;
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
    public ResponseEntity<CreateRoomResponse> createRoom(@RequestBody CreateRoomRequest request) {
        int timeLimit = request.getTimeLimit();
        if (timeLimit <= 0 || timeLimit > 360) {
            return ResponseEntity.badRequest().build(); // バリデーション例
        }

        Room room = roomService.createRoom(timeLimit);

        CreateRoomResponse response = new CreateRoomResponse(room.getRoomId(), room.getFirstPlayerId(), timeLimit, room.getStatus());
        return ResponseEntity.status(201).body(response);
    }

}
