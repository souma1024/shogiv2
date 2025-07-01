package com.souma1024.shogiv2.websocket.dto;

import com.souma1024.shogiv2.websocket.dto.enums.WebSocketType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebSocketMessage {
    
    private WebSocketType type;
    private Object payload;

    public WebSocketMessage() {}


    public WebSocketMessage(WebSocketType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }


    public WebSocketType getType() {
        return type;
    }
    
    public void setType(WebSocketType type) {
        this.type = type;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

}
