package com.souma1024.shogiv2.enums.websocket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WebSocketType {

    START_GAME_REQUEST("start_game_request"),
    START_GAME_RESPONSE("start_game_response"),
    MOVE_REQUEST("move_request"),
    MOVE_RESPONSE("move_response"),
    MOVABLE_POSITION_REQUEST("movable_position_request"),
    MOVABLE_POSITION_RESPONSE("movable_position_response"),
    GAME_OVER_REQUEST("game_over_request"),
    GAME_OVER_RESPONSE("game_over_response"),
    GAME_TIMEOUT_EVENT("game_timeout_event"),
    SERVER_ERROR_EVENT("server_error_event"),
    RECONNECT_REQUEST("reconnect_request"),
    RECONNECT_RESPONSE("reconnect_response"),
    GAME_STATE("game_state");

    private final String value;

    WebSocketType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static WebSocketType fromValue(String value) {
        for (WebSocketType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown WebSocketType: " + value);
    }

}
