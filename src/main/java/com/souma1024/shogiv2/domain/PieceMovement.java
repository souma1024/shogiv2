package com.souma1024.shogiv2.domain;

public class PieceMovement {
    private static final int[][] DIRECTION_KIN = {
        {-1, 0}, {-1, -1}, {-1, 1}, {0, -1}, {0, 1}, {1, 0}
    };

    private static final int[][] DIRECTION_FU = {
        {-1, 0}
    };

    private static final int[][] DIRECTION_GI = {
        {-1, -1}, {-1, 0}, {-1, 1}, {1, -1}, {1, 1}
    };

    private static final int[][] DIRECTION_KAKU = {
        {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    private static final int[][] DIRECTION_HISHA = {
        {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };

    private static final int[][] DIRECTION_KYO = {
        {-1, 0}
    };

    private static final int[][] DIRECTION_KEI = {
        {-2, -1}, {-2, 1}
    };

    private static final int[][] DIRECTION_GYOKU = {
        {-1, -1}, {-1, 0}, {-1, 1},
        {0, -1},          {0, 1},
        {1, -1},  {1, 0}, {1, 1}
    };


    public static int[][] getDirection(int kind) {
        boolean isPromoted = Math.abs(kind) >= 100 && Math.abs(kind) < 200;
        boolean isGote = kind < 0;
        int baseKind = toUnpromoted(Math.abs(kind));

        int[][] baseDirections = switch (baseKind) {
            case Piece.FU_SENTE     -> DIRECTION_FU;
            case Piece.KYO_SENTE    -> DIRECTION_KYO;
            case Piece.KEI_SENTE    -> DIRECTION_KEI;
            case Piece.GIN_SENTE     -> isPromoted ? DIRECTION_KIN : DIRECTION_GI;
            case Piece.KIN_SENTE    -> DIRECTION_KIN;
            case Piece.KAKU_SENTE   -> isPromoted ? merge(DIRECTION_KAKU, DIRECTION_KIN) : DIRECTION_KAKU;
            case Piece.HISYA_SENTE  -> isPromoted ? merge(DIRECTION_HISHA, DIRECTION_KIN) : DIRECTION_HISHA;
            case Piece.GYOKU_SENTE     -> DIRECTION_GYOKU;
            default -> new int[0][0];
        };

        return isGote ? flipDirections(baseDirections) : baseDirections;
    }
    
    private static int[][] flipDirections(int[][] dirs) {
        int[][] flipped = new int[dirs.length][2];
        for (int i = 0; i < dirs.length; i++) {
            flipped[i][0] = -dirs[i][0]; // y方向（上下）を反転
            flipped[i][1] = -dirs[i][1]; // x方向（左右）も反転（横駒対策）
        }
        return flipped;
    }

    private static int toUnpromoted(int kind) {
        return (kind >= 100) ? kind - 100 : kind;
    }

    private static int[][] merge(int[][] a, int[][] b) {
        int[][] result = new int[a.length + b.length][2];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

}
