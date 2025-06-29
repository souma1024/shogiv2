package com.souma1024.shogiv2.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RoomStatus {

    CREATED("created"),
    WAITING("waiting"),
    READY("ready"),
    SUCCESS("success"),
    FAILURE("failure"),
    ABORTED("aborted"),
    ENDED("ended"),
    PAUSED("paused"),
    CLOSED("closed"),
    ACTIVE("active"),
    INACTIVE("inactive");


    private final String value;

    RoomStatus(String value) {
        this.value = value;
    }

    // JSON にこの値で出力するためのアノテーション
    @JsonValue
    public String getValue() {
        return value;
    }
}
