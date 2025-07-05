package com.souma1024.shogiv2.websocket;

import java.util.Arrays;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.souma1024.shogiv2.common.enums.PlayerStatus;
import com.souma1024.shogiv2.domain.ApplyMoveResult;
import com.souma1024.shogiv2.domain.GameState;
import com.souma1024.shogiv2.domain.MovableQuery;
import com.souma1024.shogiv2.domain.PlayerSide;
import com.souma1024.shogiv2.domain.ShogiEngine;
import com.souma1024.shogiv2.dto.StartGameRequest;
import com.souma1024.shogiv2.dto.StartGameResponse;
import com.souma1024.shogiv2.model.Room;
import com.souma1024.shogiv2.repository.RoomRepository;
import com.souma1024.shogiv2.websocket.dto.*;
import com.souma1024.shogiv2.websocket.dto.enums.GameOverReason;
import com.souma1024.shogiv2.websocket.dto.enums.WebSocketType;

public class ShogiWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RoomManager roomManager = RoomManager.getInstance();
    
    private final RoomRepository roomRepository;

    
    public ShogiWebSocketHandler(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
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

    private void dispatchMessage(WebSocketSession session, WebSocketType type, JsonNode payload) throws Exception {
        switch (type) {
            case START_GAME_REQUEST -> handleStartGameRequest(convert(payload, StartGameRequest.class), session);
            case MOVABLE_POSITION_REQUEST -> handleMovablePositionRequest(convert(payload, MovablePositionRequest.class), session);
            case MOVE_REQUEST -> handleMoveRequest(convert(payload, MoveRequest.class), session);
            case GAME_OVER_REQUEST -> handleGameOverRequest(convert(payload, GameOverRequest.class), session);
            case RECONNECT_REQUEST -> handleReconnectRequest(convert(payload, ReconnectRequest.class), session);
            default -> sendError(session, "未対応のtypeです: " + type, 1003);
        }
    }

    private <T> T convert(JsonNode payload, Class<T> clazz) throws Exception {
        return mapper.treeToValue(payload, clazz);
    }

    private void handleStartGameRequest(StartGameRequest req, WebSocketSession session) throws Exception {
        String roomId = req.getRoomId();
        String playerId = req.getPlayerId();

        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));

        updatePlayerStatus(room, playerId);

        if (bothPlayersReady(room)) {
            startGame(room);
        } else {
            roomRepository.save(room);
        }
    }

    private void updatePlayerStatus(Room room, String playerId) {
        if (playerId.equals(room.getFirstPlayerId())) {
            room.setFirstPlayerStatus(PlayerStatus.ACTIVE);
        } else if (playerId.equals(room.getSecondPlayerId())) {
            room.setSecondPlayerStatus(PlayerStatus.ACTIVE);
        }
    }

    private boolean bothPlayersReady(Room room) {
        return room.getFirstPlayerStatus() == PlayerStatus.ACTIVE &&
               room.getSecondPlayerStatus() == PlayerStatus.ACTIVE;
    }

    private void startGame(Room room) throws Exception {
        String roomId = room.getRoomId();
        ShogiEngine engine = roomManager.getOrCreateEngine(roomId, room.getFirstPlayerId(), room.getSecondPlayerId());
        GameState state = engine.toGameState();

        StartGameResponse response = new StartGameResponse();
        response.setRoomId(roomId);
        response.setBoard(state.getBoard());
        response.setCapturedPieces(state.getCapturedPieces());
        response.setSenteId(room.getFirstPlayerId());
        response.setGoteId(room.getSecondPlayerId());

        roomManager.broadcastToRoom(roomId, new WebSocketMessage(WebSocketType.START_GAME_RESPONSE, response));
    }

    private void handleMovablePositionRequest(MovablePositionRequest req, WebSocketSession session) throws Exception {
        // 盤座標チェック
        if (!isValidFrom(req.getFrom())) {
            System.out.printf("❌ 無効なfrom座標: %s%n", Arrays.toString(req.getFrom()));
            return;
        }

        // ShogiEngine を取得
        ShogiEngine engine = roomManager.getEngine(req.getRoomId());
        if (engine == null) {
            System.out.printf("❌ ShogiEngineなし: roomId=%s%n", req.getRoomId());
            return;
        }

        if (!isPlayersOwnPiece(req, engine)) {
            System.out.println("❌ 他人の駒を操作しようとしました");
            return;
        }
        MovableQuery query = buildMovableQuery(req, engine);
        List<int[]> movable = (req.getFrom() != null)
            ? engine.getMovablePositions(query)
            : engine.getDropPositions(query);

        sendMovableResponse(session, req.getRoomId(), req.getPlayerId(), req.getFrom(), req.getPiece(), movable);
    }

    private boolean isValidFrom(int[] from) {
        return from == null || from.length == 2;
    }

    private boolean isPlayersOwnPiece(MovablePositionRequest req, ShogiEngine engine) {
        String playerId = req.getPlayerId();
        int piece = req.getPiece();
        PlayerSide turn = engine.getTurnPlayer();
        boolean isCurrentTurn = playerId.equals(engine.getCurrentPlayerId());

        return isCurrentTurn && ((turn == PlayerSide.SENTE && piece > 0) || (turn == PlayerSide.GOTE && piece < 0));
    }

    private MovableQuery buildMovableQuery(MovablePositionRequest req, ShogiEngine engine) {
        MovableQuery query = new MovableQuery();
        query.setFrom(req.getFrom());
        query.setPiece(req.getPiece());
        query.setPlayerId(req.getPlayerId());
        query.setPromotion(req.isPromotion());
        query.setTurn(engine.getTurnPlayer());
        return query;
    }

    private void sendMovableResponse(WebSocketSession session, String roomId, String playerId, int[] from, int piece, List<int[]> movable) throws Exception {
        MovablePositionResponse response = new MovablePositionResponse();
        response.setRoomId(roomId);
        response.setPlayerId(playerId);
        response.setFrom(from);
        response.setPiece(piece);
        response.setMovable(movable);

        send(session, WebSocketType.MOVABLE_POSITION_RESPONSE, response);
    }

    private void send(WebSocketSession session, WebSocketType type, Object payload) throws Exception {
        String json = mapper.writeValueAsString(new WebSocketMessage(type, payload));
        session.sendMessage(new TextMessage(json));
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
        String roomId = req.getRoomId();
        String playerId = req.getPlayerId();

        MoveResponse response = new MoveResponse();
        response.setRoomId(roomId);
        response.setPlayerId(playerId);
        response.setFrom(req.getFrom());
        response.setTo(req.getTo());
        response.setPiece(req.getPiece());
        response.setPromotion(req.isPromotion());
        response.setSuccess(result.isSuccess());
        response.setNextPlayerId(engine.getCurrentPlayerId());
        response.setCaptured(result.getCaptured());

        System.out.println("nextPlayerId: " + engine.getCurrentPlayerId());

        if (engine.isCheckmate(engine.getTurnPlayer())) {
            sendGameOver(roomId, playerId);
            roomManager.removeRoom(roomId);
        } else {
            broadcastToRoom(roomId, new WebSocketMessage(WebSocketType.MOVE_RESPONSE, response));
        }
    }

    private void sendGameOver(String roomId, String winnerId) throws Exception {
        GameOverResponse over = new GameOverResponse();
        over.setRoomId(roomId);
        over.setPlayerId(winnerId);
        over.setWinner(winnerId);
        over.setReason(GameOverReason.TSUMI);

        broadcastToRoom(roomId, new WebSocketMessage(WebSocketType.GAME_OVER_RESPONSE, over));
    }

    private void handleGameOverRequest(GameOverRequest req, WebSocketSession session) throws Exception {
        GameOverResponse res = new GameOverResponse();
        res.setRoomId(req.getRoomId());
        res.setPlayerId(req.getPlayerId());
        res.setWinner(req.getPlayerId());
        res.setReason(req.getReason());

        broadcastToRoom(req.getRoomId(), new WebSocketMessage(WebSocketType.GAME_OVER_RESPONSE, res));
        roomManager.removeRoom(req.getRoomId());
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

        GameStateDto state = new GameStateDto();
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
            roomManager.addSession(roomId, session);
            System.out.println("🟢 WebSocket接続: " + session.getId() + " → room " + roomId + " / player " + playerId);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String roomId = extractRoomId(session);
        if (roomId != null) {
            roomManager.removeSession(roomId, session);
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