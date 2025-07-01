package com.souma1024.shogiv2.websocket.util;

import java.util.HashMap;
import java.util.Map;

import com.souma1024.shogiv2.websocket.dto.GameOverRequest;
import com.souma1024.shogiv2.websocket.dto.MoveRequest;
import com.souma1024.shogiv2.websocket.dto.ReconnectRequest;
import com.souma1024.shogiv2.websocket.dto.enums.WebSocketType;


public class WebSocketPayloadTypeResolver {
    private static final Map<WebSocketType, Class<?>> typeMap = new HashMap<>();

    static {
        typeMap.put(WebSocketType.MOVE_REQUEST, MoveRequest.class);
        typeMap.put(WebSocketType.GAME_OVER_REQUEST, GameOverRequest.class);
        typeMap.put(WebSocketType.RECONNECT_REQUEST, ReconnectRequest.class);
        // 他のtypeも追加
    }

    public static Class<?> resolve(WebSocketType type) {
        return typeMap.get(type);
    }
}
