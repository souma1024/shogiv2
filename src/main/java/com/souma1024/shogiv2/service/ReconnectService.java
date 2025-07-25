package com.souma1024.shogiv2.service;

import org.springframework.stereotype.Service;

import com.souma1024.shogiv2.dto.websocket.request.ReconnectRequest;
import com.souma1024.shogiv2.dto.websocket.response.ReconnectResponse;
import com.souma1024.shogiv2.factory.ResponseFactory;

@Service
public class ReconnectService {

    public ReconnectService() {
    }

    public ReconnectResponse handleReconnect(ReconnectRequest req) {
        String roomId = req.getRoomId();
        String playerId = req.getPlayerId();

        System.out.println("🛠 reconnect_request: roomId = " + roomId + ", playerId = " + playerId);

        ReconnectResponse res = ResponseFactory.createReconnectResponse(req);

        return res;
    }

}
