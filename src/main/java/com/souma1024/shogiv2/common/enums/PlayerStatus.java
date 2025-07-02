package com.souma1024.shogiv2.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PlayerStatus {
    NOT_READY("not_ready"),    // 初期状態（参加はしたが未準備）
    READY("ready"),       // 「対局開始」ボタンを押した状態（WebSocket未接続）
    ACTIVE("active"),      // WebSocket接続済みでゲームに参加中
    DISCONNECTED("disconnected"), // 途中離脱など（再接続に備える場合）
    FINISHED("finished");     // 対局が終了した状態（終了後のリザルト表示など）

    private final String value;

    PlayerStatus(String value) {
        this.value = value;
    }

    // JSON にこの値で出力するためのアノテーション
    @JsonValue
    public String getValue() {
        return value;
    }
}
