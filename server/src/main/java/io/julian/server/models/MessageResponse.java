package io.julian.server.models;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class MessageResponse {
    private final static Logger log = LogManager.getLogger(MessageResponse.class.getName());
    private final JsonObject message;
    public static final String MESSAGE_KEY = "message";

    public MessageResponse(JsonObject message) {
        this.message = message;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject().put(MESSAGE_KEY, message));
    }
}
