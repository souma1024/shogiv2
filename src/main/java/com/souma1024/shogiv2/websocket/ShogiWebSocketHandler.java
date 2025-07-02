package com.souma1024.shogiv2.websocket;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.souma1024.shogiv2.common.enums.PlayerStatus;
import com.souma1024.shogiv2.common.enums.RoomStatus;
import com.souma1024.shogiv2.domain.GameState;
import com.souma1024.shogiv2.domain.Player;
import com.souma1024.shogiv2.domain.ShogiEngine;
import com.souma1024.shogiv2.dto.StartGameRequest;
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
            System.out.println("受信生データ: " + message.getPayload());
            JsonNode root = mapper.readTree(message.getPayload());
            WebSocketMessage wsMessage = mapper.readValue(message.getPayload(), WebSocketMessage.class);
            WebSocketType type = wsMessage.getType();
            JsonNode payload = root.get("payload");

            switch (type) {

                case START_GAME_REQUEST -> {
                    StartGameRequest req = mapper.treeToValue(payload, StartGameRequest.class);
                    handleStartGameRequest(req, session);
                }

                case MOVE_REQUEST -> {
                    MoveRequest req = mapper.treeToValue(payload, MoveRequest.class);
                    handleMoveRequest(req, session);
                }
                case GAME_OVER_REQUEST -> {
                    GameOverRequest req = mapper.treeToValue(payload, GameOverRequest.class);
                    handleGameOverRequest(req, session);
                }
                case RECONNECT_REQUEST -> {
                    ReconnectRequest req = mapper.treeToValue(payload, ReconnectRequest.class);
                    handleReconnectRequest(req, session);
                }
                default -> sendError(session, "未対応のtypeです: " + type, 1003);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(session, "メッセージ処理中にエラーが発生しました", 1000);
        }
    }

    private void handleStartGameRequest(StartGameRequest req, WebSocketSession session) throws Exception {
        String roomId = req.getRoomId();
        String playerId = req.getPlayerId();

        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));

        if (playerId.equals(room.getFirstPlayerId())) {
            room.setFirstPlayerStatus(PlayerStatus.ACTIVE);
        } else if (playerId.equals(room.getSecondPlayerId())) {
            room.setSecondPlayerStatus(PlayerStatus.ACTIVE);
        }

        if (room.getFirstPlayerStatus() == PlayerStatus.ACTIVE &&
            room.getSecondPlayerStatus() == PlayerStatus.ACTIVE) {

            room.setStatus(RoomStatus.ACTIVE);
            roomRepository.save(room);

            // 対局開始データを送信（仮：ShogiEngineの状態など）

            ShogiEngine engine = roomManager.getOrCreateEngine(roomId, room.getFirstPlayerId(), room.getSecondPlayerId());


            GameState state = engine.toGameState();
            
            roomManager.broadcastToRoom(roomId, new WebSocketMessage(WebSocketType.GAME_STATE, state));
        } else {
            // 片方だけACTIVEの場合も保存
            roomRepository.save(room);
        }
    }

    private void handleMoveRequest(MoveRequest req, WebSocketSession session) throws Exception {
        String roomId = req.getRoomId();
        String playerId = req.getPlayerId();

        if (!roomManager.isRoomReady(roomId)) {
            sendError(session, roomId, "ルームが開始されていません", 1004);
            return;
        }

        ShogiEngine engine = roomManager.getEngine(roomId);
        Player player = roomManager.getPlayerById(roomId, playerId);

        if (player == null) {
            sendError(session, roomId, "不正なプレイヤーID", 1005);
            return;
        }

        boolean success = engine.applyMove(req);

        MoveResponse res = new MoveResponse();
        res.setRoomId(roomId);
        res.setPlayerId(playerId);
        res.setFrom(req.getFrom());
        res.setTo(req.getTo());
        res.setKind(req.getKind());
        res.setPromotion(req.isPromotion());
        res.setSuccess(success);
        res.setNextPlayerId(engine.getCurrentPlayerId());

        if (engine.isCheckmate(engine.getTurnPlayer())) {
            GameOverResponse over = new GameOverResponse();
            over.setRoomId(roomId);
            over.setPlayerId(playerId);
            over.setWinner(playerId);
            over.setReason(GameOverReason.TSUMI);

            broadcastToRoom(roomId, new WebSocketMessage(WebSocketType.GAME_OVER_RESPONSE, over));
            roomManager.removeRoom(roomId);
        } else {
            broadcastToRoom(roomId, new WebSocketMessage(WebSocketType.MOVE_RESPONSE, res));
        }
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