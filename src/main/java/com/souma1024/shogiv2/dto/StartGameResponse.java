package com.souma1024.shogiv2.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class StartGameResponse {
    private String roomId;
    private int[][] board; // 盤面（9x9）
    private Map<String, List<Integer>> capturedPieces; // 持ち駒（playerIdごと）
    private String senteId;
    private String goteId;
    private String playerId;
}
