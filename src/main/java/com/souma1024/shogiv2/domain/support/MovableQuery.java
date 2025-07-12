package com.souma1024.shogiv2.domain.support;

import com.souma1024.shogiv2.enums.common.PlayerSide;

import lombok.Data;

@Data
public class MovableQuery {
    private int piece;
    private boolean promotion;
    private String playerId;
    private PlayerSide turn;
    private int[] from;
}
