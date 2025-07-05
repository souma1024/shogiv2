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
    private final Map<String, int[]> capturedPieces = new HashMap<>();
    private PlayerSide turn = PlayerSide.SENTE;
    private final String senteId;
    private final String goteId;

    public ShogiEngine(String senteId, String goteId) {
        this.senteId = senteId;
        this.goteId = goteId;
        initializeBoard();
        capturedPieces.put(senteId, new int[7]); // 各要素は 0 で初期化される
        capturedPieces.put(goteId, new int[7]); // 各要素は 0 で初期化される

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
            }
        } else {
            int captured = getPieceAt(to[0], to[1]);
            int actualPiece = Math.abs(captured);
            if (captured != 0) {
                capturedPieces.get(getCurrentPlayerId())[actualPiece]++;

                capturedPiece = new CapturedPiece();
                capturedPiece.setOwner(move.getPlayerId());
                capturedPiece.setPiece(captured);
                capturedPiece.setCount(capturedPieces.get(move.getPlayerId())[actualPiece]);
            }

            // 盤面に反映（成り処理含む）
            PieceUtil.applyMoveOnBoard(board, move);
        }

        // 手番交代
        turn = (turn == PlayerSide.SENTE) ? PlayerSide.GOTE : PlayerSide.SENTE;
        System.out.println("✅ 手番交代: 次は " + turn);

        return new ApplyMoveResult(true, capturedPiece);
    }

    private boolean isValidMove(MoveRequest move) {

        int[] from = move.getFrom();
        int[] to = move.getTo();
        int piece = move.getPiece();
        PlayerSide side = getTurnPlayer();

        if (from == null) {
            // 盤面・持ち駒情報を使って合法打ち位置を列挙
            int[] hand = capturedPieces.get(move.getPlayerId());
            List<int[]> legalDrops = PieceUtil.getDropPositions(board, piece, side, hand);

            boolean isLegal = legalDrops.stream()
                .anyMatch(pos -> pos[0] == to[0] && pos[1] == to[1]);

            if (!isLegal) {
                System.out.println("❌ この位置には打てません");
            }

            return isLegal;
        } 

        // 打ち駒（二歩・打ち歩詰め）
        if (PieceUtil.isNariForced(piece, to[1]) && !move.isPromotion()) {
            System.out.println("❌ 成りが強制される場面で不成です");
            return false;
        }

        if (PieceUtil.isSelfCheckAfterMove(board, move, side)) {
            System.out.println("❌ 自玉が取られる手です");
            return false;
        }

        return true;

    }

    public List<int[]> getMovablePositions(MovableQuery query) {
        int x = query.getFrom()[0];
        int y = query.getFrom()[1];
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

    public List<int[]> getDropPositions(MovableQuery query) {
        int piece = query.getPiece();
        String playerId = query.getPlayerId();
        PlayerSide side = (playerId.equals(senteId)) ? PlayerSide.SENTE : PlayerSide.GOTE;

        int[] handPieces = capturedPieces.get(playerId); // プレイヤーの持ち駒一覧

        return PieceUtil.getDropPositions(board, piece, side, handPieces);
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

    public Map<String, int[]> getCapturedPieces() {
        Map<String, int[]> copy = new HashMap<>();
        for (Map.Entry<String, int[]> entry : capturedPieces.entrySet()) {
            int[] original = entry.getValue();
            int[] cloned = Arrays.copyOf(original, original.length); // 配列を複製
            copy.put(entry.getKey(), cloned);
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