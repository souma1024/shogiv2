// package com.souma1024.shogiv2.service;

// import org.springframework.stereotype.Service;
// import org.springframework.web.socket.WebSocketSession;

// import com.souma1024.shogiv2.common.enums.PlayerStatus;
// import com.souma1024.shogiv2.domain.GameState;
// import com.souma1024.shogiv2.domain.ShogiEngine;
// import com.souma1024.shogiv2.dto.StartGameRequest;
// import com.souma1024.shogiv2.dto.StartGameResponse;
// import com.souma1024.shogiv2.factory.ResponseFactory;
// import com.souma1024.shogiv2.model.Room;
// import com.souma1024.shogiv2.repository.RoomRepository;
// import com.souma1024.shogiv2.websocket.RoomManager;
// import com.souma1024.shogiv2.websocket.dto.WebSocketMessage;
// import com.souma1024.shogiv2.websocket.dto.enums.WebSocketType;

// @Service
// public class GameStartService {
//     private final RoomRepository roomRepository;
//     private final RoomManager roomManager;

//     public GameStartService(RoomRepository roomRepository, RoomManager roomManager) {
//         this.roomRepository = roomRepository;
//         this.roomManager = roomManager;
//     }

//     public void handleStartGameRequest(StartGameRequest req, WebSocketSession session) throws Exception {
//         String roomId = req.getRoomId();
//         String playerId = req.getPlayerId();

//         Room room = roomRepository.findById(roomId)
//             .orElseThrow(() -> new RuntimeException("Room not found"));

//         updatePlayerStatus(room, playerId);

//         if (bothPlayersReady(room)) {
//             startGame(room);
//         } else {
//             roomRepository.save(room);
//         }
//     }

//     private void updatePlayerStatus(Room room, String playerId) {
//         if (playerId.equals(room.getFirstPlayerId())) {
//             room.setFirstPlayerStatus(PlayerStatus.ACTIVE);
//         } else if (playerId.equals(room.getSecondPlayerId())) {
//             room.setSecondPlayerStatus(PlayerStatus.ACTIVE);
//         }
//     }

//     private boolean bothPlayersReady(Room room) {
//         return room.getFirstPlayerStatus() == PlayerStatus.ACTIVE &&
//                room.getSecondPlayerStatus() == PlayerStatus.ACTIVE;
//     }

//     private void startGame(Room room) throws Exception {
//         String roomId = room.getRoomId();
//         ShogiEngine engine = roomManager.getOrCreateEngine(roomId, room.getFirstPlayerId(), room.getSecondPlayerId());
//         GameState state = engine.toGameState();

//         StartGameResponse response = ResponseFactory.createStartGameResponse(roomId, state, room);

//         roomManager.broadcastToRoom(roomId, new WebSocketMessage(WebSocketType.START_GAME_RESPONSE, response));
//     }
// }


