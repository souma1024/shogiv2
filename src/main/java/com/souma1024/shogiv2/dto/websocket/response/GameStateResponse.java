package com.souma1024.shogiv2.dto.websocket.response;

import java.util.Map;

public class GameStateResponse {
    private String roomId;
    private int[][] board; // 盤面（9x9）
    private Map<String, int[]> capturedPieces; // 持ち駒（playerIdごと）
    private String currentPlayerId; // 現在の手番のプレイヤーID

    // --- ゲッター ---
    public String getRoomId() {
        return roomId;
    }

    public int[][] getBoard() {
        return board;
    }

    public Map<String, int[]> getCapturedPieces() {
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

    public void setCapturedPieces(Map<String, int[]> capturedPieces) {
        this.capturedPieces = capturedPieces;
    }

    public void setCurrentPlayerId(String currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

}
