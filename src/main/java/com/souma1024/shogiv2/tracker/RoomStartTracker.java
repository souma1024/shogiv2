package com.souma1024.shogiv2.tracker;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RoomStartTracker {
    private static final RoomStartTracker instance = new RoomStartTracker();
    private final Map<String, Set<String>> readyMap = new ConcurrentHashMap<>();

    private RoomStartTracker() {}

    public static RoomStartTracker getInstance() {
        return instance;
    }

    public synchronized void markPlayerReady(String roomId, String playerId) {
        readyMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(playerId);
    }

    public synchronized boolean isBothReady(String roomId) {
        return readyMap.getOrDefault(roomId, Set.of()).size() >= 2;
    }

    public synchronized void clear(String roomId) {
        readyMap.remove(roomId);
    }
}

