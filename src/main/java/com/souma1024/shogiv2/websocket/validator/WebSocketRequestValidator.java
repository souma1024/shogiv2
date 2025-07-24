package com.souma1024.shogiv2.websocket.validator;

import com.souma1024.shogiv2.dto.websocket.common.ValidatableRequest;
import com.souma1024.shogiv2.service.GameContext;

public class WebSocketRequestValidator {

    private final GameContext gameContext;

    public WebSocketRequestValidator(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public void validate(ValidatableRequest request) {
        String roomId = request.getRoomId();
        String playerId = request.getPlayerId();

        // ルーム存在チェック
        if (!gameContext.roomExists(roomId)) {
            throw new ValidationException("ルームが存在しません");
        }

        // プレイヤーがそのルームに所属しているか
        if (!gameContext.isPlayerInRoom(roomId, playerId)) {
            throw new ValidationException("プレイヤーがルームに所属していません");
        }

        // ゲームが開始されているか（必要なら）
        if (!gameContext.isGameStarted(roomId)) {
            throw new ValidationException("対局はまだ開始されていません");
        }

        // DTO固有の追加チェック
        if (!request.validate(gameContext)) {
            throw new ValidationException("リクエストの検証に失敗しました");
        }
    }
}
