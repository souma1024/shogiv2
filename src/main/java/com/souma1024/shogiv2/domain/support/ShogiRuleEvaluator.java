package com.souma1024.shogiv2.domain.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.souma1024.shogiv2.domain.engine.ShogiEngine;
import com.souma1024.shogiv2.domain.model.Piece;
import com.souma1024.shogiv2.domain.model.PieceMovement;
import com.souma1024.shogiv2.domain.model.PieceUtil;
import com.souma1024.shogiv2.dto.websocket.request.MoveRequest;
import com.souma1024.shogiv2.enums.common.PlayerSide;

public class ShogiRuleEvaluator {

    private final ShogiEngine engine;

    public ShogiRuleEvaluator(ShogiEngine engine) {
        this.engine = engine;
    }

    /** 合法手かどうか */
    public boolean isValidMove(MoveRequest move) {
        int[][] board = engine.getBoard();
        PlayerSide side = engine.getTurnPlayer();

        int[] from = move.getFrom();
        int[] to = move.getTo();
        int piece = move.getPiece();

        if (engine.isSameOwner(to[0], to[1], side)) return false;

        if (from == null) { // 打ち駒
            int[] hand = engine.getCapturedPieces().get(move.getPlayerId());
            List<int[]> legalDrops = getDropPositions(board, piece, side, hand);
            return legalDrops.stream().anyMatch(pos -> Arrays.equals(pos, to));
        }

        if (PieceUtil.isNariForced(piece, to[1]) && !move.isPromotion()) return false;
        if (isSelfCheckAfterMove(board, move, side)) return false;

        // 動かせる位置
        List<int[]> rawMoves = getMovablePositions(board, from[0], from[1]);
        List<MoveRequest> candidates = new ArrayList<>();
        for (int[] pos : rawMoves) {
            MoveRequest candidate = new MoveRequest();
            candidate.setFrom(from);
            candidate.setTo(pos);
            candidate.setPiece(piece);
            candidate.setPromotion(move.isPromotion());
            candidate.setPlayerId(move.getPlayerId());
            candidates.add(candidate);
        }
        return getLegalMovesOnly(board, candidates, side).stream()
                .anyMatch(m -> Arrays.equals(m.getTo(), to));
    }

    /** 自玉が王手にさらされる手か */
    public static boolean isSelfCheckAfterMove(int[][] board, MoveRequest move, PlayerSide side) {
        int[][] copy = PieceUtil.copyBoard(board);
        PieceUtil.applyMoveOnBoard(copy, move); // 盤面だけ更新

        int targetKing = (side == PlayerSide.SENTE) ? Piece.GYOKU_SENTE : Piece.GYOKU_GOTE;
        int kingX = -1, kingY = -1;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (copy[y][x] == targetKing) {
                    kingX = x; kingY = y;
                }
            }
        }
        return isSquareThreatenedByOpponent(copy, kingX, kingY, side);
    }

    // --- 純粋関数群（static） ---
    public static boolean isSquareThreatenedByOpponent(int[][] board, int x, int y, PlayerSide self) {
        for (int fromY = 0; fromY < 9; fromY++) {
            for (int fromX = 0; fromX < 9; fromX++) {
                int piece = board[fromY][fromX];
                if (piece == 0) continue;

                boolean isEnemy = (PieceUtil.isSente(piece) && self == PlayerSide.GOTE)
                               || (PieceUtil.isGote(piece) && self == PlayerSide.SENTE);
                if (!isEnemy) continue;

                List<int[]> moves = getMovablePositions(board, fromX, fromY);
                for (int[] move : moves) if (move[0] == x && move[1] == y) return true;
            }
        }
        return false;
    }

    public static List<MoveRequest> getLegalMovesOnly(int[][] board, List<MoveRequest> candidates, PlayerSide side) {
        List<MoveRequest> result = new ArrayList<>();
        for (MoveRequest m : candidates) if (!isSelfCheckAfterMove(board, m, side)) result.add(m);
        return result;
    }

    public static List<int[]> getMovablePositions(int[][] board, int x, int y) {
        List<int[]> moves = new ArrayList<>();
        int piece = board[y][x];
        if (piece == 0) return moves;

        boolean isSlide = PieceUtil.isSlidePiece(piece);
        int[][] directions = PieceMovement.getDirection(piece);

        for (int[] dir : directions) {
            int dx = dir[1], dy = dir[0];
            int nx = x + dx, ny = y + dy;
            while (PieceUtil.inBoard(nx, ny)) {
                int target = board[ny][nx];
                if (target == 0 || !PieceUtil.isSameSide(piece, target)) moves.add(new int[]{nx, ny});
                if (target != 0 || !isSlide) break;
                nx += dx; ny += dy;
            }
        }
        return moves;
    }

    public static List<int[]> getDropPositions(int[][] board, int piece, PlayerSide side, int[] handPieces) {
        List<int[]> positions = new ArrayList<>();
        for (int y = 0; y < 9; y++) for (int x = 0; x < 9; x++) {
            if (board[y][x] != 0) continue;
            if (!PieceUtil.isEmpty(board, x, y)) continue;
            if (!canDropAt(board, piece, x, y, side)) continue;
            positions.add(new int[]{x, y});
        }
        return positions;
    }

    public static boolean canDropAt(int[][] board, int piece, int x, int y, PlayerSide side) {
        if (Math.abs(piece) == 1 && isNiFu(board, x, side)) return false;
        if (Math.abs(piece) == 1 && isUchiFuZume(board, x, y, side)) return false;
        if (Math.abs(piece) == 3 && ((side == PlayerSide.SENTE && y <= 1) || (side == PlayerSide.GOTE && y >= 7))) return false;
        if ((Math.abs(piece) == 1 || Math.abs(piece) == 2) && ((side == PlayerSide.SENTE && y == 0) || (side == PlayerSide.GOTE && y == 8))) return false;
        return true;
    }

    public static boolean isNiFu(int[][] board, int x, PlayerSide side) {
        int target = (side == PlayerSide.SENTE) ? 1 : -1;
        for (int y = 0; y < 9; y++) if (board[y][x] == target) return true;
        return false;
    }

    public static boolean isUchiFuZume(int[][] board, int toX, int toY, PlayerSide side) {
        // TODO: 詰将棋探索ロジック
        return false;
    }

    public List<int[]> getMovablePositions(MovableQuery query) {
        int[][] board = engine.getBoard();
        return getMovablePositions(board, query.getFrom()[0], query.getFrom()[1]);
    }

    public List<int[]> getDropPositions(MovableQuery query) {
        int[][] board = engine.getBoard();
        int[] hand = engine.getCapturedPieces().get(query.getPlayerId());
        PlayerSide side = query.getPlayerId().equals(engine.getSenteId()) ? PlayerSide.SENTE : PlayerSide.GOTE;
        return getDropPositions(board, query.getPiece(), side, hand);
    }
}