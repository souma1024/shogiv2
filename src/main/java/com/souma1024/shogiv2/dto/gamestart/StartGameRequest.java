package com.souma1024.shogiv2.dto.gamestart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartGameRequest {
    private String roomId;
    private String playerId;
}
