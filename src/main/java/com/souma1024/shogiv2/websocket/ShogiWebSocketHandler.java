package com.souma1024.shogiv2.websocket;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.souma1024.shogiv2.domain.engine.ShogiEngine;
import com.souma1024.shogiv2.domain.support.ApplyMoveResult;
import com.souma1024.shogiv2.dto.gamestart.StartGameRequest;
import com.souma1024.shogiv2.dto.gamestart.StartGameResponse;
import com.souma1024.shogiv2.dto.websocket.WebSocketMessage;
import com.souma1024.shogiv2.dto.websocket.common.ServerErrorEvent;
import com.souma1024.shogiv2.dto.websocket.request.GameOverRequest;
import com.souma1024.shogiv2.dto.websocket.request.MovablePositionRequest;
import com.souma1024.shogiv2.dto.websocket.request.MoveRequest;
import com.souma1024.shogiv2.dto.websocket.request.ReconnectRequest;
import com.souma1024.shogiv2.dto.websocket.response.GameOverResponse;
import com.souma1024.shogiv2.dto.websocket.response.GameStateResponse;
import com.souma1024.shogiv2.dto.websocket.response.MovablePositionResponse;
import com.souma1024.shogiv2.dto.websocket.response.MoveResponse;
import com.souma1024.shogiv2.dto.websocket.response.ReconnectResponse;
import com.souma1024.shogiv2.entity.Room;
import com.souma1024.shogiv2.enums.game.GameOverReason;
import com.souma1024.shogiv2.enums.websocket.WebSocketType;
import com.souma1024.shogiv2.factory.ResponseFactory;
import com.souma1024.shogiv2.repository.RoomRepository;
import com.souma1024.shogiv2.service.GameStartService;
import com.souma1024.shogiv2.service.MovablePositionService;
import com.souma1024.shogiv2.service.RoomSessionManager;

public class ShogiWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    
    private final RoomRepository roomRepository;
    private final GameStartService gameStartService;
    private final RoomSessionManager roomManager;
    private final MovablePositionService movablePositionService;

    public ShogiWebSocketHandler(RoomRepository roomRepository, GameStartService gameStartService, RoomSessionManager roomManager, MovablePositionService movablePositionService) {
        this.roomRepository = roomRepository;
        this.gameStartService = gameStartService;
        this.roomManager = roomManager;
        this.movablePositionService = movablePositionService;
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
            case MOVE_REQUEST -> handleMoveRequest(convert(payload, MoveRequest.class), session);
            case GAME_OVER_REQUEST -> handleGameOverRequest(convert(payload, GameOverRequest.class), session);
            case RECONNECT_REQUEST -> handleReconnectRequest(convert(payload, ReconnectRequest.class), session);
            default -> sendError(session, "未対応のtypeです: " + type, 1003);
        }
    }

    private <T> T convert(JsonNode payload, Class<T> clazz) throws Exception {
        return mapper.treeToValue(payload, clazz);
    }

    private void handleMoveRequest(MoveRequest req, WebSocketSession session) throws Exception {
        if (!validateMoveRequest(req, session)) return;

        ShogiEngine engine = roomManager.getEngine(req.getRoomId());
        ApplyMoveResult result = engine.applyMove(req);

        sendMoveResult(req, result, engine);
    }

    private boolean validateMoveRequest(MoveRequest req, WebSocketSession session) throws Exception {
        String roomId = req.getRoomId();
        String playerId = req.getPlayerId();

        if (!roomManager.isRoomReady(roomId)) {
            sendError(session, roomId, "ルームが開始されていません", 1004);
            return false;
        }

        if (roomManager.getPlayerById(roomId, playerId) == null) {
            sendError(session, roomId, "不正なプレイヤーID", 1005);
            return false;
        }

        return true;
    }

    private void sendMoveResult(MoveRequest req, ApplyMoveResult result, ShogiEngine engine) throws Exception {

        MoveResponse response = ResponseFactory.createMoveResponse(req, result, engine);
        
        System.out.println("nextPlayerId: " + engine.getCurrentPlayerId());

        if (engine.isCheckmate(engine.getTurnPlayer())) {
            sendGameOver(req.getRoomId(), req.getPlayerId(), GameOverReason.TSUMI);
            roomManager.removeRoom(req.getRoomId());
        } else {
            broadcastToRoom(req.getRoomId(), new WebSocketMessage(WebSocketType.MOVE_RESPONSE, response));
        }
    }

    private void sendGameOver(String roomId, String winnerId, GameOverReason reason) throws Exception {
        GameOverResponse over = ResponseFactory.createGameOverResponse(roomId, winnerId, reason);

        broadcastToRoom(roomId, new WebSocketMessage(WebSocketType.GAME_OVER_RESPONSE, over));
    }

    private void handleGameOverRequest(GameOverRequest req, WebSocketSession session) throws Exception {
        String roomId = req.getRoomId();
        String loserId = req.getPlayerId();
        String winnerId = getOpponentId(roomId, loserId); // 🔄 逆のプレイヤーIDを取得

        sendGameOver(roomId, winnerId, req.getReason());
        roomManager.removeRoom(roomId);
    }

    private String getOpponentId(String roomId, String playerId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        if (playerId.equals(room.getFirstPlayerId())) {
            return room.getSecondPlayerId();
        } else if (playerId.equals(room.getSecondPlayerId())) {
            return room.getFirstPlayerId();
        } else {
            throw new IllegalArgumentException("プレイヤーがルームに存在しません");
        }
    }


    private void handleReconnectRequest(ReconnectRequest req, WebSocketSession session) throws Exception {
        String roomId = req.getRoomId();
        String playerId = req.getPlayerId();

        System.out.println("🛠 reconnect_request: roomId = " + roomId + ", playerId = " + playerId);
        if (!roomManager.existsRoom(roomId)) {
            ReconnectResponse error = new ReconnectResponse();
            error.setRoomId(roomId);
            error.setSuccess(false);
            error.setMessage("ルームが存在しません");

            session.sendMessage(new TextMessage(mapper.writeValueAsString(
                new WebSocketMessage(WebSocketType.RECONNECT_RESPONSE, error)
            )));
            return;
        }

        // ShogiEngine は対局開始後にのみ存在
        ShogiEngine engine = roomManager.getEngine(roomId);
        if (engine == null) {
            ReconnectResponse pending = new ReconnectResponse();
            pending.setRoomId(roomId);
            pending.setSuccess(true);
            pending.setMessage("対局はまだ開始していません");

            session.sendMessage(new TextMessage(mapper.writeValueAsString(
                new WebSocketMessage(WebSocketType.RECONNECT_RESPONSE, pending)
            )));
            return;
        }

        // 成功
        ReconnectResponse ok = new ReconnectResponse();
        ok.setRoomId(roomId);
        ok.setSuccess(true);
        ok.setMessage("再接続成功");

        session.sendMessage(new TextMessage(mapper.writeValueAsString(
            new WebSocketMessage(WebSocketType.RECONNECT_RESPONSE, ok)
        )));

        GameStateResponse state = new GameStateResponse();
        state.setRoomId(roomId);
        state.setBoard(engine.getBoard());
        state.setCapturedPieces(engine.getCapturedPieces());
        state.setCurrentPlayerId(engine.getCurrentPlayerId());

        session.sendMessage(new TextMessage(mapper.writeValueAsString(
            new WebSocketMessage(WebSocketType.GAME_STATE, state)
        )));
    }

    private void broadcastToRoom(String roomId, WebSocketMessage message) throws Exception {
        String json = mapper.writeValueAsString(message);
        List<WebSocketSession> sessions = roomManager.getSessions(roomId);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
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
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2 && kv[0].equals("roomId")) {
                    return kv[1];
                }
            }
        }
        return null;
    }

    private String extractPlayerId(@NonNull WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2 && kv[0].equals("playerId")) {
                    return kv[1];
                }
            }
        }
        return null;
    }
    
}