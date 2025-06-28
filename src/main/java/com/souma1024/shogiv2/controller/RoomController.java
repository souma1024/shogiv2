package com.souma1024.shogiv2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.souma1024.shogiv2.dto.CreateRoomRequest;
import com.souma1024.shogiv2.dto.CreateRoomResponse;
import com.souma1024.shogiv2.service.RoomService;

@RestController
@RequestMapping("/api")
public class RoomController {
    
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/rooms") 
    public ResponseEntity<CreateRoomResponse> createRoom(@RequestBody CreateRoomRequest request) {
        int timeLimit = request.getTimeLimit();
        if (timeLimit <= 0 || timeLimit > 360) {
            return ResponseEntity.badRequest().build(); // バリデーション例
        }

        String roomId = roomService.generateRoomId();
        String playerId = roomService.generatePlayerId();

        CreateRoomResponse response = new CreateRoomResponse(roomId, playerId, timeLimit, "created");
        return ResponseEntity.status(201).body(response);
    }

}
