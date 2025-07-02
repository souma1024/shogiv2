package com.souma1024.shogiv2.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.souma1024.shogiv2.domain.Player;
import com.souma1024.shogiv2.domain.ShogiEngine;
import com.souma1024.shogiv2.websocket.dto.WebSocketMessage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.socket.WebSocketSession;

public class RoomManager {
    private static final RoomManager instance = new RoomManager();

    private final Map<String, List<WebSocketSession>> sessionMap = new ConcurrentHashMap<>();
    private final Map<String, ShogiEngine> engineMap = new ConcurrentHashMap<>();
    private final Map<String, Player[]> playerMap = new ConcurrentHashMap<>();
    private final Set<String> startedRooms = ConcurrentHashMap.newKeySet();

    private RoomManager() {}

    public static RoomManager getInstance() {
        return instance;
    }

    public boolean existsRoom(String roomId) {
        return playerMap.containsKey(roomId);
    }

    public synchronized ShogiEngine getOrCreateEngine(String roomId, String senteId, String goteId) {
        return engineMap.computeIfAbsent(roomId, id -> {
            startedRooms.add(roomId); // 必要に応じてここで開始フラグを立てる
            return new ShogiEngine(senteId, goteId);
        });
    }

    // プレイヤーをルームに追加
    public synchronized boolean canAddPlayer(String roomId, Player player) {
        Player[] players = playerMap.computeIfAbsent(roomId, k -> new Player[2]);
        if (players[0] == null) {
            players[0] = player;
            System.out.println("✅ Player1 参加: " + player.getId());
            return true;
        } else if (players[1] == null) {
            players[1] = player;
            System.out.println("✅ Player2 参加: " + player.getId());
            return true;
        }
        return false;
    }

    // プレイヤーIDに対応する Player を返す
    public Player getPlayerById(String roomId, String playerId) {
        Player[] players = playerMap.get(roomId);
        if (players == null) return null;
        for (Player p : players) {
            if (p != null && p.getId().equals(playerId)) {
                return p;
            }
        }
        return null;
    }

    // セッションを追加
    public void addSession(String roomId, WebSocketSession session) {
        sessionMap.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    // セッションを削除（全てのセッションが切れたらルーム削除）
    public void removeSession(String roomId, WebSocketSession session) {
        List<WebSocketSession> sessions = sessionMap.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            System.out.println("🔌 WebSocket切断: " + session.getId() + " from room " + roomId);
            if (sessions.isEmpty()) {
                removeRoom(roomId);
                System.out.println("🧹 ルーム削除: " + roomId + "（全セッション切断）");
            }
        }
    }

    public void broadcastToRoom(String roomId, WebSocketMessage message) {
        List<WebSocketSession> sessions = getSessions(roomId);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(message);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new org.springframework.web.socket.TextMessage(json));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<WebSocketSession> getSessions(String roomId) {
        List<WebSocketSession> sessions = sessionMap.get(roomId);
        return sessions == null ? List.of() : List.copyOf(sessions);
    }

    // 対局開始
    public synchronized boolean tryStartGame(String roomId) {
        if (!startedRooms.contains(roomId)) {
            Player[] players = playerMap.get(roomId);
            if (players != null && players[0] != null && players[1] != null) {
                engineMap.put(roomId, new ShogiEngine(players[0].getId(), players[1].getId()));
                startedRooms.add(roomId);
                System.out.println("🎮 対局開始: " + roomId);
                return true;
            }
        }
        return false;
    }

    public boolean isRoomReady(String roomId) {
        return startedRooms.contains(roomId);
    }

    public ShogiEngine getEngine(String roomId) {
        return engineMap.get(roomId);
    }

    public Player[] getPlayers(String roomId) {
        return playerMap.getOrDefault(roomId, new Player[2]);
    }


    public void removeRoom(String roomId) {
        startedRooms.remove(roomId);
        engineMap.remove(roomId);
        playerMap.remove(roomId);
        sessionMap.remove(roomId);
    }
}