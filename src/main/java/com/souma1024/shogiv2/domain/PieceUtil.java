package com.souma1024.shogiv2.domain;

import java.util.ArrayList;
import java.util.List;

import com.souma1024.shogiv2.websocket.dto.MoveRequest;

public class PieceUtil {
    private PieceUtil() {
        // インスタンス化禁止
    }

    private static final int BOARD_SIZE = 9;

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

    public static List<int[]> getMovablePositions(int[][] board, int x, int y) {
        List<int[]> moves = new ArrayList<>();
        int piece = board[y][x];
        boolean isSente = isSente(piece);
        int absPiece = Math.abs(toUnpromoted(piece));

        int[][] directions;
        boolean slide = false; // 長く進む駒（角・飛など）

        switch (absPiece) {
            case 1 -> directions = DIRECTION_FU;
            case 2 -> {
                directions = DIRECTION_KYO;
                slide = true;
            }
            case 3 -> directions = DIRECTION_KEI;
            case 4 -> directions = DIRECTION_GI;
            case 5 -> directions = DIRECTION_KIN;
            case 6 -> {
                directions = DIRECTION_KAKU;
                slide = true;
            }
            case 7 -> {
                directions = DIRECTION_HISHA;
                slide = true;
            }
            case 8 -> {
                directions = merge(DIRECTION_KAKU, DIRECTION_HISHA);
                slide = true;
            }
            case 9 -> {
                directions = merge(DIRECTION_HISHA, DIRECTION_KAKU);
                slide = true;
            }
            case 77 -> {
                directions = new int[][] {
                    {-1, -1}, {-1, 0}, {-1, 1},
                    {0, -1},          {0, 1},
                    {1, -1},  {1, 0}, {1, 1}
                };
            }
            default -> directions = new int[0][0];
        }
        // 例：成り歩、成り香、成り桂、成り銀は金と同じ動き
        if (isPromoted(piece)) {
            directions = DIRECTION_KIN;
            slide = false;
        }

        for (int[] dir : directions) {
            int dy = isSente ? dir[0] : -dir[0];
            int dx = isSente ? dir[1] : -dir[1];
            int ny = y + dy;
            int nx = x + dx;

            while (inBoard(nx, ny)) {
                int target = board[ny][nx];
                if (target == 0 || !isSameSide(piece, target)) {
                    moves.add(new int[]{nx, ny});
                }
                if (target != 0 || !slide) break;

                ny += dy;
                nx += dx;
            }
        }

        return moves;
    }

    private static boolean inBoard(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    private static int[][] merge(int[][] a, int[][] b) {
        int[][] result = new int[a.length + b.length][];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
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

    public static boolean isSameSide(int piece1, int piece2) {
        return (piece1 > 0 && piece2 > 0) || (piece1 < 0 && piece2 < 0);
    }

    public static String getPieceName(int piece) {
        int base = toUnpromoted(piece);
        boolean promoted = isPromoted(piece);
        boolean isSente = isSente(piece);

        String name = switch (Math.abs(base)) {
            case 1 -> "歩";
            case 2 -> "香";
            case 3 -> "桂";
            case 4 -> "銀";
            case 5 -> "金";
            case 6 -> "角";
            case 7 -> "飛";
            case 8 -> "馬";
            case 9 -> "龍";
            case 77 -> isSente ? "王" : "玉";
            default -> "？";
        };

        if (promoted && base != 5) {
            name = "成" + name;
        }

        return name;
    }

    public static boolean isNiFu(int[][] board, int x, PlayerSide playerSide) {
        for (int y = 0; y < 9; y++) {
            int piece = board[y][x];
            if (toUnpromoted(piece) == Piece.FU_SENTE && isSente(piece) == (playerSide == PlayerSide.SENTE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUchiFuZume(int[][] board, int toX, int toY, PlayerSide playerSide) {

        // 詰みチェックの一部として実装
        return false; // 本格実装はミニ探索ロジックが必要
    }

    public static boolean isNariForced(int piece, int toY) {
        int abs = Math.abs(toUnpromoted(piece));
        boolean isSente = isSente(piece);
        if ((abs == Piece.FU_SENTE || abs == Piece.KY_SENTE) && ((isSente && toY == 0) || (!isSente && toY == 8))) {
            return true;
        }
        if (abs == Piece.KE_SENTE && ((isSente && toY <= 1) || (!isSente && toY >= 7))) {
            return true;
        }
        return false;
    }

    public static boolean isSelfCheckAfterMove(int[][] board, MoveRequest move, PlayerSide playerSide) {
        // 仮想的に move を適用した盤面をコピー
        int[][] boardCopy = copyBoard(board);
        applyMoveOnBoard(boardCopy, move); // 移動・打ち・成りも含める

        // 自分の王の位置を特定
        int kingPiece = (playerSide == PlayerSide.SENTE) ? Piece.OU_SENTE : Piece.OU_GOTE;
        int kingX = -1, kingY = -1;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (boardCopy[y][x] == kingPiece) {
                    kingX = x;
                    kingY = y;
                    break;
                }
            }
        }

        // 相手の駒が、そのマスに利きを持つなら check
        return isSquareThreatenedByOpponent(boardCopy, kingX, kingY, playerSide);
    }

    public static List<MoveRequest> getLegalMovesOnly(int[][] board, List<MoveRequest> allCandidates, PlayerSide playerSide) {
        List<MoveRequest> legal = new ArrayList<>();
        for (MoveRequest move : allCandidates) {
            if (!isSelfCheckAfterMove(board, move, playerSide)) {
                legal.add(move);
            }
        }
        return legal;
    }

    public static int[][] copyBoard(int[][] board) {
        int[][] newBoard = new int[9][9];
        for (int y = 0; y < 9; y++) {
            System.arraycopy(board[y], 0, newBoard[y], 0, 9);
        }
        return newBoard;
    }

    public static void applyMoveOnBoard(int[][] board, MoveRequest move) {
        int[] from = move.getFrom();
        int[] to = move.getTo();
        int kind = move.getKind();
        boolean promote = move.isPromotion();

        int toX = to[0], toY = to[1];

        // 打ち駒（fromが [-1, -1]）
        if (from[0] == -1 && from[1] == -1) {
            board[toY][toX] = kind;
            return;
        }

        int fromX = from[0], fromY = from[1];

        int movingPiece = board[fromY][fromX];

        // 成り対応
        if (promote) {
            movingPiece = isSente(movingPiece) ? movingPiece + 100 : movingPiece - 100;
        }

        // 移動処理
        board[toY][toX] = movingPiece;
        board[fromY][fromX] = 0;
    }

    public static boolean isSquareThreatenedByOpponent(int[][] board, int targetX, int targetY, PlayerSide selfPlayer) {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                int piece = board[y][x];
                if (piece == 0) continue;

                boolean isEnemy = (isSente(piece) && selfPlayer == PlayerSide.GOTE) ||
                                (isGote(piece) && selfPlayer == PlayerSide.SENTE);
                if (!isEnemy) continue;

                List<int[]> candidateMoves = getMovablePositions(board, x, y);
                for (int[] move : candidateMoves) {
                    if (move[0] == targetX && move[1] == targetY) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isCheckmate(int[][] board, PlayerSide playerSide, List<Integer> handPieces) {
        int[][] copy = copyBoard(board);

        int kingPiece = (playerSide == PlayerSide.SENTE) ? Piece.OU_SENTE : Piece.OU_GOTE;

        // 王の座標を取得
        int kingX = -1, kingY = -1;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (copy[y][x] == kingPiece) {
                    kingX = x;
                    kingY = y;
                }
            }
        }

        // 1. 王に王手がかかっているか？
        if (!isSquareThreatenedByOpponent(copy, kingX, kingY, playerSide)) {
            return false; // 王手がかかってないなら詰んでない
        }

        // 2. 王の逃げ場チェック（合法に逃げられるか）
        List<int[]> kingMoves = getMovablePositions(copy, kingX, kingY);
        for (int[] move : kingMoves) {
            int toX = move[0], toY = move[1];
            int[][] simulated = copyBoard(copy);
            simulated[toY][toX] = kingPiece;
            simulated[kingY][kingX] = 0;

            if (!isSquareThreatenedByOpponent(simulated, toX, toY, playerSide)) {
                return false; // 王が逃げられる
            }
        }

        // 3. 他の駒で防げるか（移動 or 合駒）
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                int piece = copy[y][x];
                if (piece == 0) continue;
                if (playerSide == PlayerSide.SENTE && !isSente(piece)) continue;
                if (playerSide == PlayerSide.GOTE && isSente(piece)) continue;

                List<int[]> moves = getMovablePositions(copy, x, y);
                for (int[] move : moves) {
                    MoveRequest fakeMove = new MoveRequest();
                    fakeMove.setFrom(new int[]{x, y});
                    fakeMove.setTo(move);
                    fakeMove.setKind(piece);
                    fakeMove.setPromotion(false);
                    fakeMove.setPlayerId("dummy");

                    if (!isSelfCheckAfterMove(copy, fakeMove, playerSide)) {
                        return false; // 助ける駒がある
                    }
                }
            }
        }

        // 4. 持ち駒で防げるか（合駒）
        for (Integer handPiece : handPieces) {
            for (int y = 0; y < 9; y++) {
                for (int x = 0; x < 9; x++) {
                    if (copy[y][x] != 0) continue;

                    MoveRequest drop = new MoveRequest();
                    drop.setFrom(new int[]{-1, -1});
                    drop.setTo(new int[]{x, y});
                    drop.setKind(handPiece);
                    drop.setPromotion(false);
                    drop.setPlayerId("dummy");

                    if (!isSelfCheckAfterMove(copy, drop, playerSide)) {
                        return false; // 合駒で助けられる
                    }
                }
            }
        }

        return true; // すべての手段がない → 詰み
    }

}
