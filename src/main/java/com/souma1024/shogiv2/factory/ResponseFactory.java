package com.souma1024.shogiv2.factory;

import java.util.List;

import com.souma1024.shogiv2.domain.engine.ShogiEngine;
import com.souma1024.shogiv2.domain.model.ApplyMoveResult;
import com.souma1024.shogiv2.domain.model.GameState;
import com.souma1024.shogiv2.dto.StartGameResponse;
import com.souma1024.shogiv2.model.Room;
import com.souma1024.shogiv2.websocket.dto.GameOverResponse;
import com.souma1024.shogiv2.websocket.dto.MovablePositionResponse;
import com.souma1024.shogiv2.websocket.dto.MoveRequest;
import com.souma1024.shogiv2.websocket.dto.MoveResponse;
import com.souma1024.shogiv2.websocket.dto.ReconnectRequest;
import com.souma1024.shogiv2.websocket.dto.ReconnectResponse;
import com.souma1024.shogiv2.websocket.dto.enums.GameOverReason;

public class ResponseFactory {
    
    public static StartGameResponse createStartGameResponse(String roomId, GameState state, Room room) {
        StartGameResponse response = new StartGameResponse();
        response.setRoomId(roomId);
        response.setBoard(state.getBoard());
        response.setCapturedPieces(state.getCapturedPieces());
        response.setSenteId(room.getFirstPlayerId());
        response.setGoteId(room.getSecondPlayerId());
        return response;
    }

    public static MovablePositionResponse createMovablePositionResponse(String roomId, String playerId, int[] from, int piece, List<int[]> movable) {
        MovablePositionResponse response = new MovablePositionResponse();
        response.setRoomId(roomId);
        response.setPlayerId(playerId);
        response.setFrom(from);
        response.setPiece(piece);
        response.setMovable(movable);
        return response;
    }

    public static MoveResponse createMoveResponse(MoveRequest req, ApplyMoveResult result, ShogiEngine engine) {
        MoveResponse response = new MoveResponse();
        response.setRoomId(req.getRoomId());
        response.setPlayerId(req.getPlayerId());
        response.setFrom(req.getFrom());
        response.setTo(req.getTo());
        response.setPiece(req.getPiece());
        response.setPromotion(req.isPromotion());
        response.setSuccess(result.isSuccess());
        response.setNextPlayerId(engine.getCurrentPlayerId());
        response.setCaptured(result.getCaptured());
        return response;
    }

    public static GameOverResponse createGameOverResponse(String roomId, String winnerId, GameOverReason reason) {
        GameOverResponse response = new GameOverResponse();
        response.setRoomId(roomId);
        response.setPlayerId(winnerId);
        response.setWinner(winnerId);
        response.setReason(reason);
        return response;
    }

    public static ReconnectResponse createReconnectResponse(ReconnectRequest req) {
        ReconnectResponse response = new ReconnectResponse();
        response.setRoomId(req.getRoomId());
        response.setSuccess(false);
        response.setMessage("ルームが存在しません");
        return response;
    }

}
