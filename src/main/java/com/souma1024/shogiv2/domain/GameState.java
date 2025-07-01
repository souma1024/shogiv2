// package com.souma1024.shogiv2.domain;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import com.souma1024.shogiv2.websocket.dto.MoveRequest;

// public class GameState {
//      // 駒の種類を表す整数 or Enum
//     private int[][] board = new int[9][9]; // 盤面
//     private Player turn = Player.SENTE;   // 手番
//     private Map<Player, List<Integer>> capturedPieces = new HashMap<>(); // 持ち駒
//     private boolean isGameOver = false;
//     private String sentePlayerId;
//     private String gotePlayerId;

//     public boolean applyMove(MoveRequest move) {
//         if (isGameOver) return false;

//         // 手番の確認
//         Player currentPlayer = this.turn;
//         if (!isCurrentPlayer(move.getPlayerId())) {
//             return false;
//         }

//         int fromY = move.getFrom()[1];
//         int fromX = move.getFrom()[0];
//         int toY = move.getTo()[1];
//         int toX = move.getTo()[0];
//         int piece = move.getKind();
//         boolean promote = move.isPromotion();

//         // from: -1,-1 の場合 → 持ち駒から打つ
//         boolean isDrop = (fromX == -1 && fromY == -1);

//         // 駒を打つ場合
//         if (isDrop) {
//             if (!capturedPieces.get(currentPlayer).contains(piece)) return false;
//             board[toY][toX] = piece;
//             capturedPieces.get(currentPlayer).remove((Integer) piece);
//         } else {
//             // 移動元にその駒があるか
//             if (board[fromY][fromX] != piece) return false;

//             // 相手の駒がいる場合 → 持ち駒に追加（反転して）
//             int captured = board[toY][toX];
//             if (captured != 0) {
//                 int capturedToHand = captured * -1; // 相手の駒 → 自分用に反転
//                 capturedPieces.get(currentPlayer).add(capturedToHand);
//             }

//             // 成り処理（仮：単純に+100）
//             int finalPiece = promote ? promotePiece(piece) : piece;

//             board[toY][toX] = finalPiece;
//             board[fromY][fromX] = 0;
//         }

//         this.turn = (this.turn == Player.SENTE) ? Player.GOTE : Player.SENTE;
//         return true;
//     }

//     private boolean isCurrentPlayer(String playerId) {
//         return (turn == Player.SENTE && playerId.equals(sentePlayerId)) ||(turn == Player.GOTE && playerId.equals(gotePlayerId));
//     }

//     private int promotePiece(int piece) {
//         // 仮実装：+100で成りを表現（22 = 歩 → 122 = と金）
//         return piece + 100;
//     }
// }