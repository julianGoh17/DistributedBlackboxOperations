package io.julian.gossip.components;

import io.julian.gossip.models.MessageUpdate;
import io.julian.server.api.exceptions.NoIDException;
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
    private final ConcurrentHashSet<String> inactiveIds;
    private final ConcurrentHashSet<String> deletedIds;

    public State(final MessageStore messages, final ConcurrentLinkedQueue<CoordinationMessage> deadLetters) {
        this.messages = messages;
        this.deadLetters = deadLetters;
        this.inactiveIds = new ConcurrentHashSet<>();
        this.deletedIds = new ConcurrentHashSet<>();
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
        if (!deletedIds.contains(messageId)) {
            try {
                messages.addMessageToServer(messageId, message);
            } catch (final SameIDException e) {
                log.info(String.format("Skipping adding '%s' message to server", messageId));
            }
        } else {
            log.info(String.format("Skipping adding '%s' message as it is a deleted key", messageId));
        }
        log.traceExit();
    }

    public void deleteMessageIfInDatabase(final String messageId) {
        log.traceEntry(() -> messageId);
        log.info(String.format("Attempting to delete '%s'", messageId));
        if (messages.hasUUID(messageId)) {
            addDeletedId(messageId);
            try {
                messages.deleteMessageFromServer(messageId);
            } catch (final NoIDException e) {
                log.info(String.format("Did not delete '%s' as it is already deleted", messageId));
            }
        } else {
            log.info(String.format("Did not delete '%s' as it is already deleted", messageId));
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

    public JsonArray getDeletedArray() {
        log.traceEntry();
        final JsonArray array = new JsonArray();
        deletedIds.forEach(array::add);
        return log.traceExit(array);
    }

    public void addDeletedId(final String id) {
        log.traceEntry(() -> id);
        deletedIds.add(id);
        log.traceExit();
    }

    public boolean isDeletedId(final String id) {
        log.traceEntry(() -> id);
        return log.traceExit(deletedIds.contains(id));
    }

    public ConcurrentHashSet<String> getDeletedIds() {
        log.traceEntry();
        return log.traceExit(deletedIds);
    }

    public void addInactiveId(final String id) {
        log.traceEntry(() -> id);
        inactiveIds.add(id);
        log.traceExit();
    }

    public boolean isInactiveId(final String id) {
        log.traceEntry(() -> id);
        return log.traceExit(inactiveIds.contains(id));
    }

    public ConcurrentHashSet<String> getInactiveIds() {
        log.traceEntry();
        return log.traceExit(inactiveIds);
    }
}
