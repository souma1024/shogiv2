package com.souma1024.shogiv2.domain.engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.souma1024.shogiv2.domain.model.GameState;
import com.souma1024.shogiv2.domain.model.Piece;
import com.souma1024.shogiv2.domain.model.PieceUtil;
import com.souma1024.shogiv2.domain.support.ApplyMoveResult;
import com.souma1024.shogiv2.domain.support.MovableQuery;
import com.souma1024.shogiv2.dto.websocket.common.CapturedPiece;
import com.souma1024.shogiv2.dto.websocket.request.MoveRequest;
import com.souma1024.shogiv2.enums.common.PlayerSide;
import com.souma1024.shogiv2.domain.support.ShogiRuleEvaluator;

public class ShogiEngine {
    private final int[][] board = new int[9][9];
    private final Map<String, int[]> capturedPieces = new HashMap<>();
    private PlayerSide turn = PlayerSide.SENTE;
    private final String senteId;
    private final String goteId;
    private final ShogiRuleEvaluator evaluator;

    public ShogiEngine(String senteId, String goteId) {
        this.senteId = senteId;
        this.goteId = goteId;
        initializeBoard();
        capturedPieces.put(senteId, new int[7]); // 各要素は 0 で初期化される
        capturedPieces.put(goteId, new int[7]); // 各要素は 0 で初期化される
        this.evaluator = new ShogiRuleEvaluator(this);
    }

    public ShogiRuleEvaluator getEvaluator() {
        return evaluator;
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

        if (!evaluator.isValidMove(move)) {
            System.out.println("❌ 不正な手です");
            return new ApplyMoveResult(false, null);
        }

        if (isSame(move)) {
            return new ApplyMoveResult(false, null);
        }

        CapturedPiece capturedPiece = null; 
        int[] to = move.getTo();
        

        if (move.getFrom() == null) {
            int piece = move.getPiece();
            PlayerSide side = getTurnPlayer();
            int actualPiece = (side == PlayerSide.SENTE) ? piece : -piece;
            

            // 盤上に置く
            if (board[to[1]][to[0]] != 0) {
                System.out.println("❌ 打ち先にすでに駒が存在します");
                return new ApplyMoveResult(false, null);
            }

            board[to[1]][to[0]] = actualPiece;

            // 持ち駒から1個消費
            int[] hand = capturedPieces.get(move.getPlayerId());
            if (hand[actualPiece] == 0) {
                System.out.println("❌ 持ち駒に指定された駒がありません");
                return new ApplyMoveResult(false, null);
            } else {
                hand[actualPiece]--;
                capturedPiece = new CapturedPiece();
                capturedPiece.setOwner(move.getPlayerId());
                capturedPiece.setPiece(piece);
                capturedPiece.setCount(hand[actualPiece]);
            }
        } else {
            int captured = getPieceAt(to[0], to[1]);
            int actualPiece = Math.abs(PieceUtil.toUnpromoted(captured));
            if (captured != 0) {
                capturedPieces.get(getCurrentPlayerId())[actualPiece]++;

                capturedPiece = new CapturedPiece();
                capturedPiece.setOwner(move.getPlayerId());
                capturedPiece.setPiece(-1 * captured);
                capturedPiece.setCount(capturedPieces.get(move.getPlayerId())[actualPiece]);
            }

            // 盤面に反映（成り処理含む）
            PieceUtil.applyMoveOnBoard(board, move);
        }

        // 手番交代
        switchTurn();
        System.out.println("✅ 手番交代: 次は " + turn);

        return new ApplyMoveResult(true, capturedPiece);
    }

    public boolean isSameOwner(int x, int y, PlayerSide side) {
        return (getPieceAt(x, y) > 0 && side == PlayerSide.SENTE) || (getPieceAt(x, y) < 0 && side == PlayerSide.GOTE);
    }

    public PlayerSide getTurnPlayer() {
        return turn;
    }

    public String getSenteId() {
        return senteId;
    }

    public String getGoteId() {
        return goteId;
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

    public Map<String, int[]> getCapturedPieces() {
        return deepCopyCapturedPieces();
    }

    private int getPieceAt(int x, int y) {
        return board[y][x];
    }

    private boolean isSame(MoveRequest request) {
        return Arrays.equals(request.getFrom(), request.getTo());
    }

    private void switchTurn() {
        turn = (turn == PlayerSide.SENTE) ? PlayerSide.GOTE : PlayerSide.SENTE;
    }

    private Map<String, int[]> deepCopyCapturedPieces() {
        Map<String, int[]> copy = new HashMap<>();
        for (Map.Entry<String, int[]> entry : capturedPieces.entrySet()) {
            copy.put(entry.getKey(), Arrays.copyOf(entry.getValue(), entry.getValue().length));
        }
        return copy;
    }

    public List<int[]> getMovablePositions(MovableQuery query) {
        return evaluator.getMovablePositions(query);
    }

    public List<int[]> getDropPositions(MovableQuery query) {
        return evaluator.getDropPositions(query);
    }

}