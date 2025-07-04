package com.souma1024.shogiv2.domain;

import java.util.ArrayList;
import java.util.List;

import com.souma1024.shogiv2.websocket.dto.MoveRequest;

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

    private static boolean inBoard(int x, int y) {
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

    // --- 盤面操作 ---
    public static int[][] copyBoard(int[][] board) {
        int[][] newBoard = new int[9][9];
        for (int y = 0; y < 9; y++) {
            System.arraycopy(board[y], 0, newBoard[y], 0, 9);
        }
        return newBoard;
    }

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

    public static boolean isNiFu(int[][] board, int x, PlayerSide side) {
        for (int y = 0; y < 9; y++) {
            int p = board[y][x];
            if (toUnpromoted(p) == Piece.FU_SENTE && isSente(p) == (side == PlayerSide.SENTE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUchiFuZume(int[][] board, int toX, int toY, PlayerSide side) {
        // TODO: 実装が難しいため仮置き。将来的に詰将棋探索ロジック導入
        return false;
    }

    // --- 王手・詰み関連 ---
    public static boolean isSelfCheckAfterMove(int[][] board, MoveRequest move, PlayerSide playerSide) {
        int[][] copy = copyBoard(board);
        applyMoveOnBoard(copy, move);

        int targetPiece = (playerSide == PlayerSide.SENTE) ? Piece.GYOKU_SENTE : Piece.GYOKU_GOTE;
        int kingX = -1, kingY = -1;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (copy[y][x] == targetPiece) {
                    kingX = x;
                    kingY = y;
                }
            }
        }

        return isSquareThreatenedByOpponent(copy, kingX, kingY, playerSide);
    }

    public static void applyMoveOnBoard(int[][] board, MoveRequest move) {
        int[] from = move.getFrom();
        int[] to = move.getTo();
        int kind = move.getPiece();
        boolean promote = move.isPromotion();

        int toX = to[0], toY = to[1];

        // 打ち駒（持ち駒を盤面に置く） [-1, -1] ならば持ち駒を打つ
        if (from[0] == -1 && from[1] == -1) {
            board[toY][toX] = kind;
            return;
        }

        int fromX = from[0], fromY = from[1];
        int movingPiece = board[fromY][fromX];

        // 成りがある場合は、駒の値を変更
        if (promote) {
            movingPiece = isSente(movingPiece) ? movingPiece + 100 : movingPiece - 100;
        }

        // 移動先に駒を置き、元の位置は空に
        board[toY][toX] = movingPiece;
        board[fromY][fromX] = 0;
    }


    public static boolean isSquareThreatenedByOpponent(int[][] board, int x, int y, PlayerSide self) {
        for (int fromY = 0; fromY < 9; fromY++) {
            for (int fromX = 0; fromX < 9; fromX++) {
                int piece = board[fromY][fromX];
                if (piece == 0) continue;

                boolean isEnemy = (isSente(piece) && self == PlayerSide.GOTE) ||
                                  (isGote(piece) && self == PlayerSide.SENTE);
                if (!isEnemy) continue;

                List<int[]> moves = getMovablePositions(board, fromX, fromY);
                for (int[] move : moves) {
                    if (move[0] == x && move[1] == y) return true;
                }
            }
        }
        return false;
    }

    public static boolean isCheckmate(int[][] board, PlayerSide side, List<Integer> handPieces) {
        // TODO: 実装済みのものを移植して統合
        return false;
    }

    // --- 合法手フィルタリング ---
    public static List<MoveRequest> getLegalMovesOnly(int[][] board, List<MoveRequest> candidates, PlayerSide side) {
        List<MoveRequest> result = new ArrayList<>();
        for (MoveRequest m : candidates) {
            if (!isSelfCheckAfterMove(board, m, side)) {
                result.add(m);
            }
        }
        return result;
    }

    public static List<int[]> getMovablePositions(int[][] board, int x, int y) {
        List<int[]> moves = new ArrayList<>();
        int piece = board[y][x];
        if (piece == 0) return moves;

        boolean isSlide = isSlidePiece(piece);

        int[][] directions = PieceMovement.getDirection(piece);

        for (int[] dir : directions) {
            int dx = dir[1];
            int dy = dir[0];
            int nx = x + dx;
            int ny = y + dy;

            while (inBoard(nx, ny)) {
                int target = board[ny][nx];
                if (target == 0 || !isSameSide(piece, target)) {
                    moves.add(new int[] { nx, ny });
                }

                if (target != 0 || !isSlide) break;

                nx += dx;
                ny += dy;
            }
        }

        return moves;
    }
}

