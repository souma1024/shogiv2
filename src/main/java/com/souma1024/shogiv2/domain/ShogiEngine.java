package com.souma1024.shogiv2.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.souma1024.shogiv2.websocket.dto.CapturedPiece;
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
        board[0][0] = Piece.KYO_GOTE;
        board[0][1] = Piece.KEI_GOTE;
        board[0][2] = Piece.GIN_GOTE;
        board[0][3] = Piece.KIN_GOTE;
        board[0][4] = Piece.GYOKU_GOTE;
        board[0][5] = Piece.KIN_GOTE;
        board[0][6] = Piece.GIN_GOTE;
        board[0][7] = Piece.KEI_GOTE;
        board[0][8] = Piece.KYO_GOTE;
        board[1][1] = Piece.HISYA_GOTE;
        board[1][7] = Piece.KAKU_GOTE;
        for (int i = 0; i < 9; i++) {
            board[2][i] = Piece.FU_GOTE;
        }

        board[8][0] = Piece.KYO_SENTE;
        board[8][1] = Piece.KEI_SENTE;
        board[8][2] = Piece.GIN_SENTE;
        board[8][3] = Piece.KIN_SENTE;
        board[8][4] = Piece.GYOKU_SENTE;
        board[8][5] = Piece.KIN_SENTE;
        board[8][6] = Piece.GIN_SENTE;
        board[8][7] = Piece.KEI_SENTE;
        board[8][8] = Piece.KYO_SENTE;
        board[7][7] = Piece.HISYA_SENTE;
        board[7][1] = Piece.KAKU_SENTE;
        for (int i = 0; i < 9; i++) {
            board[6][i] = Piece.FU_SENTE;
        }
    }

    public GameState toGameState() {
        return new GameState(
            getBoard(),
            getCapturedPieces(),
            getCurrentPlayerId()
        );
    }

    public ApplyMoveResult applyMove(MoveRequest move) {

        if (!move.getPlayerId().equals(getCurrentPlayerId())) {
            System.out.println("❌ 手番ではないプレイヤーの手です");
            return new ApplyMoveResult(false, null);
        }

        if (!isValidMove(move)) {
            System.out.println("❌ 不正な手です");
            return new ApplyMoveResult(false, null);
        }

        if (isSame(move)) {
            return new ApplyMoveResult(false, null);
        }

        CapturedPiece capturedPiece = null; 

        int[] to = move.getTo();
        int captured = getPieceAt(to[0], to[1]);
        if (captured != 0) {
            int toHand = PieceUtil.toUnpromoted(captured) * -1;
            capturedPieces.get(getCurrentPlayerId()).add(toHand);

            capturedPiece = new CapturedPiece();
            capturedPiece.setOwner(move.getPlayerId());
            capturedPiece.setPiece(captured);
            capturedPiece.setCount(1);
        }

        // 駒を動かす（成り処理は PieceUtil 内で判断）
        PieceUtil.applyMoveOnBoard(board, move);

        // 手番交代
        turn = (turn == PlayerSide.SENTE) ? PlayerSide.GOTE : PlayerSide.SENTE;
        System.out.println("✅ 手番交代: 次は " + turn);

        return new ApplyMoveResult(true, capturedPiece);
    }

    private boolean isValidMove(MoveRequest move) {
        // 打ち駒（二歩・打ち歩詰め）
        if (move.getFrom()[0] == -1 && move.getFrom()[1] == -1) {
            int toX = move.getTo()[0];
            if (PieceUtil.toUnpromoted(move.getPiece()) == Piece.FU_SENTE &&
                PieceUtil.isNiFu(board, toX, turn)) {
                return false;
            }
            if (PieceUtil.isUchiFuZume(board, toX, move.getTo()[1], turn)) {
                return false;
            }
        }

        // 成り強制
        if (PieceUtil.isNariForced(move.getPiece(), move.getTo()[1])) {
            return false; // 成りが強制されてるのにしない手は不正
        }

        // 自玉が取られる手
        if (PieceUtil.isSelfCheckAfterMove(board, move, turn)) {
            return false;
        }

        return true;
    }

    public List<int[]> getMovablePositions(MovableQuery query) {
        int x = query.getX();
        int y = query.getY();
        int piece = query.getPiece();

        List<int[]> rawMoves = PieceUtil.getMovablePositions(board, x, y);

        List<MoveRequest> allCandidates = new ArrayList<>();

        for (int[] move : rawMoves) {
            MoveRequest m = new MoveRequest();
            m.setFrom(new int[]{x, y});
            m.setTo(move);
            m.setPiece(piece);
            m.setPromotion(false);
            m.setPlayerId(query.getPlayerId());
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

    public String getNextPlayerId() {
        return (turn == PlayerSide.SENTE) ? goteId : senteId;
    }

    public int[][] getBoard() {
        return PieceUtil.copyBoard(board);
    }

    public Map<String, List<Integer>> getCapturedPieces() {
        Map<String, List<Integer>> copy = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : capturedPieces.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    private int getPieceAt(int x, int y) {
        return board[y][x];
    }

    private boolean isSame(MoveRequest request) {
        return Arrays.equals(request.getFrom(), request.getTo());
    }

}