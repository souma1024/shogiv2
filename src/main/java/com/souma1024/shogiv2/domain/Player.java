package com.souma1024.shogiv2.domain;

public class Player {
    private final String id;           // DBから渡されたプレイヤーID
    private final PlayerSide side;     // SENTE or GOTE

    public Player(String id, PlayerSide side) {
        this.id = id;
        this.side = side;
    }

    public String getId() {
        return id;
    }

    public PlayerSide getSide() {
        return side;
    }

    public boolean isSente() {
        return side == PlayerSide.SENTE;
    }

    public boolean isGote() {
        return side == PlayerSide.GOTE;
    } 
} 
