package com.souma1024.shogiv2.domain;

public class Piece {
    // --- 歩 ---
    public static final int FU_SENTE = 1;
    public static final int FU_GOTE = -1;

    // --- 香車 ---
    public static final int KY_SENTE = 2;
    public static final int KY_GOTE = -2;

    // --- 桂馬 ---
    public static final int KE_SENTE = 3;
    public static final int KE_GOTE = -3;

    // --- 銀将 ---
    public static final int GI_SENTE = 4;
    public static final int GI_GOTE = -4;

    // --- 金将 ---
    public static final int KI_SENTE = 5;
    public static final int KI_GOTE = -5;

    // --- 角 ---
    public static final int KA_SENTE = 6;
    public static final int KA_GOTE = -6;

    // --- 飛車 ---
    public static final int HI_SENTE = 7;
    public static final int HI_GOTE = -7;

    // --- 馬（角の成り） ---
    public static final int UM_SENTE = 8;
    public static final int UM_GOTE = -8;

    // --- 龍（飛の成り） ---
    public static final int RY_SENTE = 9;
    public static final int RY_GOTE = -9;

    // --- 王将・玉将（特別扱い） ---
    public static final int OU_SENTE = 77; // 王
    public static final int OU_GOTE = -77; // 玉

    // --- 成り駒（+100ルール） ---
    public static final int NARI_FU_SENTE = FU_SENTE + 100; // = 101
    public static final int NARI_FU_GOTE = FU_GOTE - 100;   // = -101

    public static final int NARI_KY_SENTE = KY_SENTE + 100;
    public static final int NARI_KY_GOTE = KY_GOTE - 100;

    public static final int NARI_KE_SENTE = KE_SENTE + 100;
    public static final int NARI_KE_GOTE = KE_GOTE - 100;

    public static final int NARI_GI_SENTE = GI_SENTE + 100;
    public static final int NARI_GI_GOTE = GI_GOTE - 100;

    // 金将は成らないので NARI_KI はなし

    // 成り角 = 馬、成り飛 = 龍（すでに定義済み）

    private Piece() {
        // インスタンス化禁止
    }
}
