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

    public void removeMessage(final String uuid) {
        log.traceEntry(() -> uuid);

        if (hasUUID(uuid)) {
            log.info(String.format("Removing message with UUID '%s' from server", uuid));
            messages.remove(uuid);
        } else {
            log.info(String.format("Could not find with UUID '%s' to remove from server", uuid));
        }

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
