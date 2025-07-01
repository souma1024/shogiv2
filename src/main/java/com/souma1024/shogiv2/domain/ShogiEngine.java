package com.souma1024.shogiv2.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.souma1024.shogiv2.websocket.dto.MoveRequest;

public class ShogiEngine {
    private final int[][] board = new int[9][9];
    private final Map<String, List<Integer>> capturedPieces = new HashMap<>();
    private PlayerSide turn = PlayerSide.SENTE;
    private final String senteId;
    private final String goteId;

    public ShogiEngine(String senteId, String goteId) {
        this.senteId = senteId;
        this.goteId = goteId;
        initializeBoard();
        capturedPieces.put(senteId, new ArrayList<>());
        capturedPieces.put(goteId, new ArrayList<>());
    }

    private void initializeBoard() {
        board[0][0] = Piece.KY_GOTE;
        board[0][1] = Piece.KE_GOTE;
        board[0][2] = Piece.GI_GOTE;
        board[0][3] = Piece.KI_GOTE;
        board[0][4] = Piece.OU_GOTE;
        board[0][5] = Piece.KI_GOTE;
        board[0][6] = Piece.GI_GOTE;
        board[0][7] = Piece.KE_GOTE;
        board[0][8] = Piece.KY_GOTE;
        board[1][1] = Piece.HI_GOTE;
        board[1][7] = Piece.KA_GOTE;
        for (int i = 0; i < 9; i++) {
            board[2][i] = Piece.FU_GOTE;
        }

        board[8][0] = Piece.KY_SENTE;
        board[8][1] = Piece.KE_SENTE;
        board[8][2] = Piece.GI_SENTE;
        board[8][3] = Piece.KI_SENTE;
        board[8][4] = Piece.OU_SENTE;
        board[8][5] = Piece.KI_SENTE;
        board[8][6] = Piece.GI_SENTE;
        board[8][7] = Piece.KE_SENTE;
        board[8][8] = Piece.KY_SENTE;
        board[7][7] = Piece.HI_SENTE;
        board[7][1] = Piece.KA_SENTE;
        for (int i = 0; i < 9; i++) {
            board[6][i] = Piece.FU_SENTE;
        }
    }

    public boolean applyMove(MoveRequest move) {
        if (!move.getPlayerId().equals(getCurrentPlayerId())) return false;

        // 不正手チェック
        if (!isValidMove(move)) return false;

        int[] to = move.getTo();
        int captured = getPieceAt(to[0], to[1]);
        if (captured != 0) {
            int toHand = PieceUtil.toUnpromoted(captured) * -1;
            capturedPieces.get(getCurrentPlayerId()).add(toHand);
        }

        // 駒を動かす（成り処理は PieceUtil 内で判断）
        PieceUtil.applyMoveOnBoard(board, move);

        // 手番交代
        turn = (turn == PlayerSide.SENTE) ? PlayerSide.GOTE : PlayerSide.SENTE;

        return true;
    }

    private boolean isValidMove(MoveRequest move) {
        // 打ち駒（二歩・打ち歩詰め）
        if (move.getFrom()[0] == -1 && move.getFrom()[1] == -1) {
            int toX = move.getTo()[0];
            if (PieceUtil.toUnpromoted(move.getKind()) == Piece.FU_SENTE &&
                PieceUtil.isNiFu(board, toX, turn)) {
                return false;
            }
            if (PieceUtil.isUchiFuZume(board, toX, move.getTo()[1], turn)) {
                return false;
            }
        }

        // 成り強制
        if (PieceUtil.isNariForced(move.getKind(), move.getTo()[1])) {
            return false; // 成りが強制されてるのにしない手は不正
        }

        // 自玉が取られる手
        if (PieceUtil.isSelfCheckAfterMove(board, move, turn)) {
            return false;
        }

        return true;
    }

    public List<int[]> getMovablePositions(MoveRequest request) {
        int x = request.getFrom()[0];
        int y = request.getFrom()[1];
        int kind = request.getKind();

        List<int[]> rawMoves = PieceUtil.getMovablePositions(board, x, y);
        List<MoveRequest> allCandidates = new ArrayList<>();

        for (int[] move : rawMoves) {
            MoveRequest m = new MoveRequest();
            m.setFrom(new int[]{x, y});
            m.setTo(move);
            m.setKind(kind);
            m.setPromotion(false);
            m.setPlayerId(request.getPlayerId());
            allCandidates.add(m);
        }

        return PieceUtil.getLegalMovesOnly(board, allCandidates, turn)
                        .stream()
                        .map(MoveRequest::getTo)
                        .toList();
    }

    public boolean isCheckmate(PlayerSide playerSide) {
        String playerId = (playerSide == PlayerSide.SENTE) ? senteId : goteId;
        return PieceUtil.isCheckmate(board, playerSide, capturedPieces.get(playerId));
    }

    public PlayerSide getTurnPlayer() {
        return turn;
    }

    public String getCurrentPlayerId() {
        return (turn == PlayerSide.SENTE) ? senteId : goteId;
    }

    public int[][] getBoard() {
        return PieceUtil.copyBoard(board);
    }

    public List<Integer> getCapturedPieces(Player player) {
        return new ArrayList<>(capturedPieces.getOrDefault(player.getId(), new ArrayList<>()));
    }

    private int getPieceAt(int x, int y) {
        return board[y][x];
    }
}