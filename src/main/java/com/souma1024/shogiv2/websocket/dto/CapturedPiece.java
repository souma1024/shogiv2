package com.souma1024.shogiv2.websocket.dto;

import lombok.Data;

@Data
public class CapturedPiece {
    private String owner;
    private int piece;
    private int count;
}
