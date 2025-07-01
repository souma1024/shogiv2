package com.souma1024.shogiv2.websocket;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.souma1024.shogiv2.domain.Player;
import com.souma1024.shogiv2.domain.ShogiEngine;
import com.souma1024.shogiv2.dto.StartGameRequest;
import com.souma1024.shogiv2.dto.StartGameResponse;
import com.souma1024.shogiv2.websocket.dto.*;
import com.souma1024.shogiv2.websocket.dto.enums.GameOverReason;
import com.souma1024.shogiv2.websocket.dto.enums.WebSocketType;

public class ShogiWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RoomManager roomManager = RoomManager.getInstance();

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

        Player player = roomManager.getPlayerById(roomId, playerId);
        if (player == null) {
            sendError(session, roomId, "無効なプレイヤーIDです", 1006);
            return;
        }

        boolean started = roomManager.startGame(roomId);

        if (started) {
            StartGameResponse res = new StartGameResponse();
            res.setRoomId(roomId);
            res.setStatus("started");

            WebSocketMessage msg = new WebSocketMessage(WebSocketType.START_GAME_RESPONSE, res);

            // 💡 対局開始通知をルーム内の全セッションに送信
            broadcastToRoom(roomId, msg);
        } else {
            // まだもう一人が準備していない場合 → 応答不要（または待機通知も可能）
            System.out.println("⚠️ 片方のみ startGameRequest。もう一人待機中。");
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
        ReconnectResponse res = new ReconnectResponse();
        res.setRoomId(req.getRoomId());
        res.setCurrentState("game_in_progress");
        res.setSuccess(true);

        session.sendMessage(new TextMessage(mapper.writeValueAsString(
            new WebSocketMessage(WebSocketType.RECONNECT_RESPONSE, res)
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