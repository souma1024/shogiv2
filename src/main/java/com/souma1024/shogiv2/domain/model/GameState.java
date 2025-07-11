package com.souma1024.shogiv2.domain.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameState {
    private int[][] board;
    private Map<String, int[]> capturedPieces;
    private String currentPlayerId;
}