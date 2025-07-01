package com.souma1024.shogiv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StartGameRequest {
    private String roomId;
    private String playerId;
}
