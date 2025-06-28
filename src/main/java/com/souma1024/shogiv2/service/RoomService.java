package com.souma1024.shogiv2.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class RoomService {
    public String generateRoomId() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    public String generatePlayerId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
