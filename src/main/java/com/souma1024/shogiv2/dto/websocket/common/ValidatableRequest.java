package com.souma1024.shogiv2.dto.websocket.common;

import com.souma1024.shogiv2.service.GameContext;

import lombok.Data;

@Data
public abstract class ValidatableRequest {
    private String roomId;
    private String playerId;

    public abstract boolean validate(GameContext context);
}
