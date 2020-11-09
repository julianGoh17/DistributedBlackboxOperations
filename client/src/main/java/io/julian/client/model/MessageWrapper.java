package io.julian.client.model;

import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageWrapper {
    private static final Logger log = LogManager.getLogger(MessageWrapper.class.getName());
    private static final String MESSAGE_KEY = "message";
    private final JsonObject wrapper;

    public MessageWrapper(final JsonObject message) {
        wrapper = new JsonObject().put(MESSAGE_KEY, message);
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(wrapper);
    }
}
