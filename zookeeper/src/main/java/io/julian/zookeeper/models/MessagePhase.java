package io.julian.zookeeper.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.julian.server.components.MessageStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum MessagePhase {
    ACK("ack"),
    COMMIT("commit"),
    ERROR("error");

    private final static Logger log = LogManager.getLogger(MessageStore.class.getName());
    private final String str;

    MessagePhase(final String str) {
        this.str = str;
    }

    @JsonCreator
    public static MessagePhase fromValue(final String str) {
        log.traceEntry(() -> str);
        switch (str) {
            case "ack":
                return log.traceExit(MessagePhase.ACK);
            case "commit":
                return log.traceExit(MessagePhase.COMMIT);
            default:
                return log.traceExit(MessagePhase.ERROR);
        }
    }

    @JsonValue
    public String toValue() {
        log.traceEntry();
        return log.traceExit(this.str);
    }
}
