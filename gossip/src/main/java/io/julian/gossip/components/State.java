package io.julian.gossip.components;

import io.julian.gossip.models.MessageUpdate;
import io.julian.server.api.exceptions.SameIDException;
import io.julian.server.components.MessageStore;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

public class State {
    private final static Logger log = LogManager.getLogger(State.class);
    private final MessageStore messages;
    private final ConcurrentLinkedQueue<CoordinationMessage> deadLetters;
    private final ConcurrentHashSet<String> inactiveKeys;

    public State(final MessageStore messages, final ConcurrentLinkedQueue<CoordinationMessage> deadLetters) {
        this.messages = messages;
        this.deadLetters = deadLetters;
        this.inactiveKeys = new ConcurrentHashSet<>();
    }

    public MessageStore getMessages() {
        log.traceEntry();
        return log.traceExit(messages);
    }

    public void addToDeadLetters(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        deadLetters.add(message);
        log.traceExit();
    }

    public void addMessageIfNotInDatabase(final String messageId, final JsonObject message) {
        log.traceEntry(() -> messageId, () -> message);
        try {
            messages.addMessageToServer(messageId, message);
        } catch (final SameIDException e) {
            log.info(String.format("Skipping adding '%s' message to server", messageId));
        }
        log.traceExit();
    }

    /*
     * Exposed for testing
     */
    public JsonArray getMessageArray() {
        log.traceEntry();
        final JsonArray array = new JsonArray();
        messages.getMessages()
            .forEach((id, message) -> array.add(new MessageUpdate(id, message).toJson()));
        return log.traceExit(array);
    }

    public void addInactiveKey(final String key) {
        log.traceEntry(() -> key);
        inactiveKeys.add(key);
        log.traceExit();
    }

    public boolean isAnInactiveKey(final String key) {
        log.traceEntry(() -> key);
        return log.traceExit(inactiveKeys.contains(key));
    }

    public ConcurrentHashSet<String> getInactiveKeys() {
        log.traceEntry();
        return log.traceExit(inactiveKeys);
    }
}
