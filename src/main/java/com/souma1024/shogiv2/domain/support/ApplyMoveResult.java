package com.souma1024.shogiv2.domain.support;

import com.souma1024.shogiv2.dto.websocket.common.CapturedPiece;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApplyMoveResult {
    private boolean success;
    private CapturedPiece captured; // null if no capture

}
