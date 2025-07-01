package com.souma1024.shogiv2.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.souma1024.shogiv2.dto.StartGameResponse;
import com.souma1024.shogiv2.dto.game.RoomStartTracker;
import com.souma1024.shogiv2.websocket.RoomManager;
import com.souma1024.shogiv2.websocket.dto.WebSocketMessage;
import com.souma1024.shogiv2.websocket.dto.enums.WebSocketType;


@RestController
@RequestMapping("/api/rooms")
public class GameStartController {
    private final RoomManager roomManager = RoomManager.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    // プレイヤーが対局開始ボタンを押した時のエンドポイント
    @PostMapping("/{roomId}/start")
    public ResponseEntity<StartGameResponse> startGame(@PathVariable String roomId, @RequestBody Map<String, String> body) throws Exception {
        String playerId = body.get("playerId");

        RoomStartTracker tracker = RoomStartTracker.getInstance();
        tracker.markPlayerReady(roomId, playerId);

        StartGameResponse response = new StartGameResponse();
        response.setPlayerId(playerId);

        if (tracker.isBothReady(roomId)) {
            // 対局開始の条件がそろったとき
            roomManager.tryStartGame(roomId);

            // 両プレイヤーに WebSocket で通知
            StartGameResponse wsResponse = new StartGameResponse();
            wsResponse.setPlayerId(playerId);
            wsResponse.setStatus("started");

            WebSocketMessage message = new WebSocketMessage(WebSocketType.START_GAME_RESPONSE, wsResponse);
            String json = mapper.writeValueAsString(message);

            for (WebSocketSession session : roomManager.getSessions(roomId)) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }

            response.setStatus("started");
        } else {
            response.setStatus("waiting");
        }

        return ResponseEntity.ok(response);
    }
}
