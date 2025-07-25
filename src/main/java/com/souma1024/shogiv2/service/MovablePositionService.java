package com.souma1024.shogiv2.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.souma1024.shogiv2.domain.engine.ShogiEngine;
import com.souma1024.shogiv2.domain.support.MovableQuery;
import com.souma1024.shogiv2.dto.websocket.request.MovablePositionRequest;
import com.souma1024.shogiv2.dto.websocket.response.MovablePositionResponse;
import com.souma1024.shogiv2.enums.common.PlayerSide;
import com.souma1024.shogiv2.factory.ResponseFactory;

@Service
public class MovablePositionService {
    private final RoomSessionManager roomManager;

    public MovablePositionService(RoomSessionManager roomManager) {
        this.roomManager = roomManager;
    }

    public MovablePositionResponse handleRequest(MovablePositionRequest req) throws Exception {
        
        ShogiEngine engine = roomManager.getEngine(req.getRoomId());
        if (engine == null) {
            throw new IllegalStateException("ShogiEngineが存在しません: " + req.getRoomId());
        }

        if (!isPlayersOwnPiece(req, engine)) {
            throw new IllegalArgumentException("他人の駒を操作しようとしました");
        }


        MovableQuery query = buildMovableQuery(req, engine);
        List<int[]> movable = (req.getFrom() != null)
            ? engine.getMovablePositions(query)
            : engine.getDropPositions(query);

        return ResponseFactory.createMovablePositionResponse(
                req.getRoomId(),
                req.getPlayerId(),
                req.getFrom(),
                req.getPiece(),
                movable
        );
    }

    private boolean isPlayersOwnPiece(MovablePositionRequest req, ShogiEngine engine) {
        String playerId = req.getPlayerId();
        int piece = req.getPiece();
        PlayerSide turn = engine.getTurnPlayer();
        boolean isCurrentTurn = playerId.equals(engine.getCurrentPlayerId());

        return isCurrentTurn && ((turn == PlayerSide.SENTE && piece > 0) || (turn == PlayerSide.GOTE && piece < 0));
    }


    private MovableQuery buildMovableQuery(MovablePositionRequest req, ShogiEngine engine) {
        MovableQuery query = new MovableQuery();
        query.setFrom(req.getFrom());
        query.setPiece(req.getPiece());
        query.setPlayerId(req.getPlayerId());
        query.setPromotion(req.isPromotion());
        query.setTurn(engine.getTurnPlayer());
        return query;
    }
}
