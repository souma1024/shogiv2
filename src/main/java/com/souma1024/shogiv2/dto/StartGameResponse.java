package com.souma1024.shogiv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StartGameResponse {
    private String status; // "waiting_for_opponent" または "started"
}
