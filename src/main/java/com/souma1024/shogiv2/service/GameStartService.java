package com.souma1024.shogiv2.service;

import org.springframework.stereotype.Service;

import com.souma1024.shogiv2.domain.engine.ShogiEngine;
import com.souma1024.shogiv2.domain.model.GameState;
import com.souma1024.shogiv2.dto.gamestart.StartGameRequest;
import com.souma1024.shogiv2.dto.gamestart.StartGameResponse;
import com.souma1024.shogiv2.entity.Room;
import com.souma1024.shogiv2.enums.common.PlayerStatus;
import com.souma1024.shogiv2.enums.common.RoomStatus;
import com.souma1024.shogiv2.factory.ResponseFactory;
import com.souma1024.shogiv2.repository.RoomRepository;
import com.souma1024.shogiv2.websocket.validator.ValidationException;


@Service
public class GameStartService {
    private final RoomRepository roomRepository;
    private final RoomSessionManager roomManager;

    public GameStartService(RoomRepository roomRepository, RoomSessionManager roomManager) {
        this.roomRepository = roomRepository;
        this.roomManager = roomManager;
    }

    /** バリデーション */
    public void validateStartGame(StartGameRequest req) {
        Room room = roomRepository.findById(req.getRoomId())
            .orElseThrow(() -> new ValidationException("ルームが存在しません"));

        if (room.getFirstPlayerId() == null || room.getSecondPlayerId() == null) {
            throw new ValidationException("プレイヤーが揃っていません");
        }
        if (room.getFirstPlayerStatus() != PlayerStatus.READY ||
            room.getSecondPlayerStatus() != PlayerStatus.READY) {
            throw new ValidationException("プレイヤーが準備完了ではありません");
        }
        if (roomManager.isRoomReady(req.getRoomId())) {
            throw new ValidationException("すでにゲームが開始されています");
        }
    }


    /** ゲーム開始処理 */
    public StartGameResponse startGame(StartGameRequest req) {
        validateStartGame(req);

        Room room = roomRepository.findById(req.getRoomId()).orElseThrow();

        // ステータスをACTIVEに
        room.setFirstPlayerStatus(PlayerStatus.ACTIVE);
        room.setSecondPlayerStatus(PlayerStatus.ACTIVE);
        room.setStatus(RoomStatus.ACTIVE);
        roomRepository.save(room);

        // エンジン初期化
        ShogiEngine engine = roomManager.getOrCreateEngine(room.getRoomId(), room.getFirstPlayerId(), room.getSecondPlayerId());
        GameState state = engine.toGameState();

        // レスポンス生成
        return ResponseFactory.createStartGameResponse(room.getRoomId(), state, room);
    }
}