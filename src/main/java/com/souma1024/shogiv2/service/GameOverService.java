package com.souma1024.shogiv2.service;

import org.springframework.stereotype.Service;
import com.souma1024.shogiv2.domain.model.Player;
import com.souma1024.shogiv2.dto.websocket.request.GameOverRequest;
import com.souma1024.shogiv2.dto.websocket.response.GameOverResponse;
import com.souma1024.shogiv2.factory.ResponseFactory;

@Service
public class GameOverService {
    private final RoomSessionManager roomManager;

    public GameOverService(RoomSessionManager roomManager) {
        this.roomManager = roomManager;
    }

    public GameOverResponse handleGameOver(GameOverRequest req) {
        String roomId = req.getRoomId();
        String loserId = req.getPlayerId();
        String winnerId = getOpponentId(roomId, loserId);

        GameOverResponse over = ResponseFactory.createGameOverResponse(roomId, winnerId, req.getReason());

        return over;
    }

    private String getOpponentId(String roomId, String playerId) {
        Player[] players = roomManager.getPlayers(roomId);
        if (players[0] != null && players[0].getId().equals(playerId)) {
            return players[1] != null ? players[1].getId() : null;
        } else if (players[1] != null && players[1].getId().equals(playerId)) {
            return players[0] != null ? players[0].getId() : null;
        }
        throw new IllegalArgumentException("プレイヤーがルームに存在しません");
    }

}
