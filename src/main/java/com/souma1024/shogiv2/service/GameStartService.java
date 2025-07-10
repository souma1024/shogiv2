package com.souma1024.shogiv2.service;

import org.springframework.stereotype.Service;

import com.souma1024.shogiv2.repository.RoomRepository;
import com.souma1024.shogiv2.websocket.RoomManager;

@Service
public class GameStartService {
    private final RoomRepository roomRepository;
    private final RoomManager roomManager;

    public GameStartService(RoomRepository roomRepository, RoomManager roomManager) {
        this.roomRepository = roomRepository;
        this.roomManager = roomManager;
    }

    

}


