package com.souma1024.shogiv2.domain.model;

import com.souma1024.shogiv2.dto.websocket.request.MoveRequest;

public class PieceUtil {
    private PieceUtil() {
        // インスタンス化禁止
    }

    public static boolean isSente(int piece) {
        return piece > 0;
    }

    public static boolean isGote(int piece) {
        return piece < 0;
    }

    public static boolean isPromoted(int piece) {
        return Math.abs(piece) >= 100 && Math.abs(piece) < 200;
    }

    public static int toUnpromoted(int piece) {
        if (!isPromoted(piece)) return piece;
        return piece > 0 ? piece - 100 : piece + 100;
    }

    public static boolean isSameSide(int p1, int p2) {
        return (p1 > 0 && p2 > 0) || (p1 < 0 && p2 < 0);
    }

    public static boolean inBoard(int x, int y) {
        return x >= 0 && x < 9 && y >= 0 && y < 9;
    }

    public static boolean isSlidePiece(int piece) {
        int base = toUnpromoted(Math.abs(piece));
        return switch (base) {
            case Piece.KYO_SENTE, Piece.KAKU_SENTE, Piece.HISYA_SENTE,
                Piece.UMA_SENTE, Piece.RYU_SENTE -> true;
            default -> false;
        };
    }

    public static int[][] copyBoard(int[][] board) {
        int[][] newBoard = new int[9][9];
        for (int y = 0; y < 9; y++) {
            System.arraycopy(board[y], 0, newBoard[y], 0, 9);
        }
        return newBoard;
    }

    //成り強制
    public static boolean isNariForced(int piece, int toY) {
        int abs = Math.abs(toUnpromoted(piece));
        boolean isSente = isSente(piece);
        if ((abs == Piece.FU_SENTE || abs == Piece.KYO_SENTE) &&
            ((isSente && toY == 0) || (!isSente && toY == 8))) {
            return true;
        }
        if (abs == Piece.KEI_SENTE &&
            ((isSente && toY <= 1) || (!isSente && toY >= 7))) {
            return true;
        }
        return false;
    }

    public static boolean isEmpty(int[][] board, int x, int y) {
        return board[y][x] == 0;
    }

    public static void applyMoveOnBoard(int[][] board, MoveRequest move) {
        int[] from = move.getFrom();
        int[] to = move.getTo();
        int piece = move.getPiece();
        boolean promote = move.isPromotion();

        if (from == null) {
            board[to[1]][to[0]] = piece;
            return;
        }

        int movingPiece = board[from[1]][from[0]];
        if (promote) movingPiece = isSente(movingPiece) ? movingPiece + 100 : movingPiece - 100;

        board[to[1]][to[0]] = movingPiece;
        board[from[1]][from[0]] = 0;
    }
}