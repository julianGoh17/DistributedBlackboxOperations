package io.julian.server.components;

import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class MessageStore {
    private static final Logger log = LogManager.getLogger(MessageStore.class.getName());
    private final Map<String, JsonObject> messages;

    public MessageStore() {
        messages = new HashMap<>();
    }

    public JsonObject getMessage(final String uuid) {
        log.traceEntry(() -> uuid);
        log.info("Getting message from Message Store");
        if (messages.containsKey(uuid)) {
            return log.traceExit(messages.get(uuid));
        }
        log.traceExit();
        return null;
    }

    public void putMessage(final String uuid, final JsonObject message) {
        log.traceEntry(() -> uuid, () -> message);
        messages.put(uuid, message);
        log.traceExit();
    }

    public boolean hasUUID(final String uuid) {
        log.traceEntry(() -> uuid);
        return log.traceExit(messages.containsKey(uuid));
    }

    public int getNumberOfMessages() {
        log.traceEntry();
        return log.traceExit(messages.size());
    }
}
