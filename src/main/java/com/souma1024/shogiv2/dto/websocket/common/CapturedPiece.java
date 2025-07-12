package com.souma1024.shogiv2.dto.websocket.common;

import lombok.Data;

@Data
public class CapturedPiece {
    private String owner;
    private int piece;
    private int count;
}
