package com.souma1024.shogiv2.websocket;


import java.net.URI;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.souma1024.shogiv2.dto.gamestart.StartGameRequest;
import com.souma1024.shogiv2.dto.gamestart.StartGameResponse;
import com.souma1024.shogiv2.dto.websocket.WebSocketMessage;
import com.souma1024.shogiv2.dto.websocket.common.ServerErrorEvent;
import com.souma1024.shogiv2.dto.websocket.request.GameOverRequest;
import com.souma1024.shogiv2.dto.websocket.request.MovablePositionRequest;
import com.souma1024.shogiv2.dto.websocket.request.MoveRequest;
import com.souma1024.shogiv2.dto.websocket.request.ReconnectRequest;
import com.souma1024.shogiv2.dto.websocket.response.GameOverResponse;
import com.souma1024.shogiv2.dto.websocket.response.MovablePositionResponse;
import com.souma1024.shogiv2.dto.websocket.response.MoveResponse;
import com.souma1024.shogiv2.dto.websocket.response.ReconnectResponse;
import com.souma1024.shogiv2.enums.websocket.WebSocketType;
import com.souma1024.shogiv2.service.GameOverService;
import com.souma1024.shogiv2.service.GameStartService;
import com.souma1024.shogiv2.service.MovablePositionService;
import com.souma1024.shogiv2.service.MoveService;
import com.souma1024.shogiv2.service.ReconnectService;
import com.souma1024.shogiv2.service.RoomSessionManager;

public class ShogiWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    
    private final GameStartService gameStartService;
    private final RoomSessionManager roomManager;
    private final MovablePositionService movablePositionService;
    private final MoveService moveService;
    private final GameOverService gameOverService;
    private final ReconnectService reconnectService;

    public ShogiWebSocketHandler(GameStartService gameStartService, RoomSessionManager roomManager, MovablePositionService movablePositionService, MoveService moveService, GameOverService gameOverService, ReconnectService reconnectService) {
        this.gameStartService = gameStartService;
        this.roomManager = roomManager;
        this.movablePositionService = movablePositionService;
        this.moveService = moveService;
        this.gameOverService = gameOverService;
        this.reconnectService = reconnectService;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        try {
            JsonNode root = mapper.readTree(message.getPayload());
            String typeString = root.get("type").asText();
            WebSocketType type = WebSocketType.fromValue(typeString);
            JsonNode payload = root.get("payload");
            dispatchMessage(session, type, payload);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(session, "メッセージ処理中にエラーが発生しました", 1000);
        }
    }

    //リクエストのタイプによって呼び出すメソッドを変える
    private void dispatchMessage(WebSocketSession session, WebSocketType type, JsonNode payload) throws Exception {
        switch (type) {
            case START_GAME_REQUEST -> {
                StartGameRequest request = convert(payload, StartGameRequest.class);
                StartGameResponse response = gameStartService.startGame(request);
                roomManager.broadcastToRoom(response.getRoomId(), new WebSocketMessage(WebSocketType.START_GAME_RESPONSE, response));
            }
            case MOVABLE_POSITION_REQUEST -> {
                MovablePositionRequest request = convert(payload, MovablePositionRequest.class);
                MovablePositionResponse response = movablePositionService.handleRequest(request);
                roomManager.sendToPlayer(response.getRoomId(), response.getPlayerId(), new WebSocketMessage(WebSocketType.MOVABLE_POSITION_RESPONSE, response));
            }
            case MOVE_REQUEST -> {
                MoveRequest request = convert(payload, MoveRequest.class);
                MoveResponse response = moveService.handleMove(request);
                roomManager.broadcastToRoom(response.getRoomId(), new WebSocketMessage(WebSocketType.MOVE_RESPONSE, response));
            }
            case GAME_OVER_REQUEST -> {
                GameOverRequest request = convert(payload, GameOverRequest.class);
                GameOverResponse response = gameOverService.handleGameOver(request);
                roomManager.broadcastToRoom(response.getRoomId(), new WebSocketMessage(WebSocketType.GAME_OVER_RESPONSE, response));
            }
            case RECONNECT_REQUEST -> {
                ReconnectRequest request = convert(payload, ReconnectRequest.class);
                ReconnectResponse response = reconnectService.handleReconnect(request);
                roomManager.broadcastToRoom(response.getRoomId(), new WebSocketMessage(WebSocketType.RECONNECT_RESPONSE, response));
            }
            default -> sendError(session, "未対応のtypeです: " + type, 1003);
        }
    }

    private <T> T convert(JsonNode payload, Class<T> clazz) throws Exception {
        return mapper.treeToValue(payload, clazz);
    }


    private void sendError(WebSocketSession session, String msg, int code) throws Exception {
        sendError(session, "unknown", msg, code);
    }

    private void sendError(WebSocketSession session, String roomId, String msg, int code) throws Exception {
        ServerErrorEvent error = new ServerErrorEvent();
        error.setRoomId(roomId);
        error.setReason("server_error");
        error.setCode(code);
        error.setMessage(msg);

        session.sendMessage(new TextMessage(mapper.writeValueAsString(
            new WebSocketMessage(WebSocketType.SERVER_ERROR_EVENT, error)
        )));
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String roomId = extractRoomId(session);
        String playerId = extractPlayerId(session);
        if (roomId != null && playerId != null) {
            roomManager.addSession(roomId, playerId, session);
            System.out.println("🟢 WebSocket接続: " + session.getId() + " → room " + roomId + " / player " + playerId);
        } else {
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String roomId = extractRoomId(session);
        String playerId = extractPlayerId(session);
        if (roomId != null) {
            roomManager.removeSession(roomId, playerId);
        }
    }

    private String extractRoomId(@NonNull WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }
        
        String query = uri.getQuery();
        if (query == null) {
            return null;
        }
        
        for (String param : query.split("&")) {
            String[] kv = param.split("=");
            if (kv.length == 2 && kv[0].equals("roomId")) {
                return kv[1];
            }
        }
        
        return null;
    }

    private String extractPlayerId(@NonNull WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }

        String query = uri.getQuery();

        if (query == null) {
            return null;
        }
   
        for (String param : query.split("&")) {
            String[] kv = param.split("=");
            if (kv.length == 2 && kv[0].equals("playerId")) {
                return kv[1];
            }
        }
        return null;
    }
    
}