package com.souma1024.shogiv2.domain;

import lombok.Data;

@Data
public class MovableQuery {
    private int x;
    private int y;
    private int kind;
    private boolean promotion;
    private String playerId;
    private PlayerSide turn;
}
