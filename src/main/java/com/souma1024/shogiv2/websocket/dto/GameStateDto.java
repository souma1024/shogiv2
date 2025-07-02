package com.souma1024.shogiv2.websocket.dto;

import java.util.List;
import java.util.Map;

public class GameStateDto {
    private String roomId;
    private int[][] board; // 盤面（9x9）
    private Map<String, List<Integer>> capturedPieces; // 持ち駒（playerIdごと）
    private String currentPlayerId; // 現在の手番のプレイヤーID

    // --- ゲッター ---
    public String getRoomId() {
        return roomId;
    }

    public int[][] getBoard() {
        return board;
    }

    public Map<String, List<Integer>> getCapturedPieces() {
        return capturedPieces;
    }

    public String getCurrentPlayerId() {
        return currentPlayerId;
    }

    // --- セッター ---
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public void setCapturedPieces(Map<String, List<Integer>> capturedPieces) {
        this.capturedPieces = capturedPieces;
    }

    public void setCurrentPlayerId(String currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

}
