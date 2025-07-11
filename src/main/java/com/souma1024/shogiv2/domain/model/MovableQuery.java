package com.souma1024.shogiv2.domain.model;

import lombok.Data;

@Data
public class MovableQuery {
    private int piece;
    private boolean promotion;
    private String playerId;
    private PlayerSide turn;
    private int[] from;
}
