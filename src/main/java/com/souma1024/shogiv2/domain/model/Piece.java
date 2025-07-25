package com.souma1024.shogiv2.domain.model;

public class Piece {
    // --- 歩 ---
    public static final int FU_SENTE = 1;
    public static final int FU_GOTE = -1;

    // --- 香車 ---
    public static final int KYO_SENTE = 2;
    public static final int KYO_GOTE = -2;

    // --- 桂馬 ---
    public static final int KEI_SENTE = 3;
    public static final int KEI_GOTE = -3;

    // --- 銀将 ---
    public static final int GIN_SENTE = 4;
    public static final int GIN_GOTE = -4;

    // --- 金将 ---
    public static final int KIN_SENTE = 5;
    public static final int KIN_GOTE = -5;

    // --- 角 ---
    public static final int KAKU_SENTE = 6;
    public static final int KAKU_GOTE = -6;

    // --- 飛車 ---
    public static final int HISYA_SENTE = 7;
    public static final int HISYA_GOTE = -7;

    // --- 王将・玉将（特別扱い） ---
    public static final int GYOKU_SENTE = 77; // 王
    public static final int GYOKU_GOTE = -77; // 玉

    // --- 成り駒（+100ルール） ---
    public static final int NARI_FU_SENTE = FU_SENTE + 100; // = 101
    public static final int NARI_FU_GOTE = FU_GOTE - 100;   // = -101

    public static final int NARI_KY_SENTE = KYO_SENTE + 100;
    public static final int NARI_KY_GOTE = KYO_GOTE - 100;

    public static final int NARI_KE_SENTE = KEI_SENTE + 100;
    public static final int NARI_KE_GOTE = KEI_GOTE - 100;

    public static final int NARI_GI_SENTE = GIN_SENTE + 100;
    public static final int NARI_GI_GOTE = GIN_GOTE - 100;

    // --- 馬（角の成り） ---
    public static final int UMA_SENTE = KAKU_SENTE + 100;
    public static final int UMA_GOTE = KAKU_GOTE - 100;

    // --- 龍（飛の成り） ---
    public static final int RYU_SENTE = HISYA_SENTE + 100;
    public static final int RYU_GOTE = HISYA_GOTE - 100;

    // 金将は成らないので NARI_KI はなし

    
    private Piece() {
        // インスタンス化禁止
    }
}
