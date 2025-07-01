package com.souma1024.shogiv2.websocket.dto.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WebSocketType {

    @JsonProperty("start_game_request")
    START_GAME_REQUEST,

    @JsonProperty("start_game_response")
    START_GAME_RESPONSE,

    @JsonProperty("move_request")
    MOVE_REQUEST,

    @JsonProperty("move_response")
    MOVE_RESPONSE,

    @JsonProperty("movable_position_request")
    MOVABLE_POSITION_REQUEST,

    @JsonProperty("movable_position_response")
    MOVABLE_POSITION_RESPONSE,

    @JsonProperty("game_over_request")
    GAME_OVER_REQUEST,

    @JsonProperty("game_over_response")
    GAME_OVER_RESPONSE,

    @JsonProperty("game_timeout_event")
    GAME_TIMEOUT_EVENT,

    @JsonProperty("server_error_event")
    SERVER_ERROR_EVENT,

    @JsonProperty("reconnect_request")
    RECONNECT_REQUEST,

    @JsonProperty("reconnect_response")
    RECONNECT_RESPONSE
}
