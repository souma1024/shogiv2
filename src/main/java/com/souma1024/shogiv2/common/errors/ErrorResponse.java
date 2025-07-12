package com.souma1024.shogiv2.common.errors;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;       // HTTPステータスコード（例: 400）
    private String error;     // エラー名（例: Bad Request）
    private String message;   // 詳細な説明（例: すでに2人参加しています）
    private String path;      // どのAPIか（例: /api/rooms/abc123/join）
}
