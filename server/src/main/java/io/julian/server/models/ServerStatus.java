package io.julian.server.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum ServerStatus {
    AVAILABLE("available"),
    UNREACHABLE("unreachable"),
    UNAVAILABLE("unavailable"),
    UNKNOWN("unknown");

    private final String str;
    ServerStatus(final String str) {
        this.str = str;
    }

    public static final Logger log = LogManager.getLogger(ServerStatus.class.getName());

    @JsonCreator
    public static ServerStatus forValue(final String str) {
        log.traceEntry(() -> str);
        switch (str) {
            case "available":
                return log.traceExit(AVAILABLE);
            case "unreachable":
                return log.traceExit(UNREACHABLE);
            case "unavailable":
                return log.traceExit(UNAVAILABLE);
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
