package com.souma1024.shogiv2.service;

import org.springframework.stereotype.Service;

import com.souma1024.shogiv2.domain.engine.ShogiEngine;
import com.souma1024.shogiv2.domain.model.Player;
import com.souma1024.shogiv2.enums.common.PlayerSide;

@Service
public class GameContext {
    private final RoomSessionManager roomSessionManager;

    public GameContext(RoomSessionManager roomSessionManager) {
        this.roomSessionManager = roomSessionManager;
    }

    /** ルームが存在するか？ */
    public boolean roomExists(String roomId) {
        return roomSessionManager.existsRoom(roomId);
    }

    /** プレイヤーがルームに属しているか？ */
    public boolean isPlayerInRoom(String roomId, String playerId) {
        Player[] players = roomSessionManager.getPlayers(roomId);
        if (players == null) return false;
        return (players[0] != null && players[0].getId().equals(playerId)) ||
               (players[1] != null && players[1].getId().equals(playerId));
    }

    /** プレイヤーの先後を取得 */
    public PlayerSide getPlayerSide(String roomId, String playerId) {
        Player[] players = roomSessionManager.getPlayers(roomId);
        if (players == null) return null;
        if (players[0] != null && players[0].getId().equals(playerId)) return PlayerSide.SENTE;
        if (players[1] != null && players[1].getId().equals(playerId)) return PlayerSide.GOTE;
        return null;
    }

    /** 現在の手番プレイヤーIDを取得 */
    public String getCurrentTurnPlayer(String roomId) {
        ShogiEngine engine = roomSessionManager.getEngine(roomId);
        if (engine == null) return null;
        PlayerSide currentTurn = engine.getTurnPlayer();
        Player[] players = roomSessionManager.getPlayers(roomId);
        if (players == null) return null;

        return (currentTurn == PlayerSide.SENTE) ? players[0].getId() : players[1].getId();
    }

    /** ゲームが開始されているか？ */
    public boolean isGameStarted(String roomId) {
        return roomSessionManager.isRoomReady(roomId);
    }

    /** ShogiEngine を取得 */
    public ShogiEngine getEngine(String roomId) {
        return roomSessionManager.getEngine(roomId);
    }

    /** プレイヤー情報を取得 */
    public Player getPlayer(String roomId, String playerId) {
        return roomSessionManager.getPlayerById(roomId, playerId);
    }
}