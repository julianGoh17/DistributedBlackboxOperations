package io.julian.server.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum HTTPRequest {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    UNKNOWN("UNKNOWN");

    private final static Logger log = LogManager.getLogger(HTTPRequest.class.getName());
    private final String str;

    HTTPRequest(final String str) {
        this.str = str;
    }

    @JsonCreator
    public static HTTPRequest forValue(final String str) {
        log.traceEntry(() -> str);
        switch (str) {
            case "GET":
                return log.traceExit(GET);
            case "POST":
                return log.traceExit(POST);
            case "PUT":
                return log.traceExit(PUT);
            case "DELETE":
                return log.traceExit(DELETE);
            default:
                return log.traceExit(UNKNOWN);
        }
    }

    @JsonValue
    public String toValue() {
        log.traceEntry();
        return log.traceExit(this.str);
    }
}
