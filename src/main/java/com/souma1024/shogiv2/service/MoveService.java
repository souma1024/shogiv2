package com.souma1024.shogiv2.service;

import org.springframework.stereotype.Service;

import com.souma1024.shogiv2.domain.engine.ShogiEngine;
import com.souma1024.shogiv2.domain.support.ApplyMoveResult;
import com.souma1024.shogiv2.dto.websocket.request.MoveRequest;
import com.souma1024.shogiv2.dto.websocket.response.MoveResponse;
import com.souma1024.shogiv2.factory.ResponseFactory;

@Service
public class MoveService {

    private final RoomSessionManager roomManager;

    public MoveService(RoomSessionManager roomManager) {
        this.roomManager = roomManager;
    } 

    public MoveResponse handleMove(MoveRequest req) {
        // ルーム状態チェック
        if (!roomManager.isRoomReady(req.getRoomId())) {
            throw new IllegalStateException("ルームが開始されていません");
        }
        if (roomManager.getPlayerById(req.getRoomId(), req.getPlayerId()) == null) {
            throw new IllegalArgumentException("不正なプレイヤーID");
        }

        ShogiEngine engine = roomManager.getEngine(req.getRoomId());
        if (engine == null) {
            throw new IllegalStateException("ShogiEngineが存在しません");
        }

        // 指し手適用
        ApplyMoveResult result = engine.applyMove(req);


        // 通常の指し手レスポンス
        return ResponseFactory.createMoveResponse(req, result, engine);
    }
}
