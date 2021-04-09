package io.julian.server.components;

import io.julian.server.api.exceptions.NoIDException;
import io.julian.server.api.exceptions.SameIDException;
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

    public void addMessageToServer(final String messageID, final JsonObject message) throws SameIDException {
        log.traceEntry(() -> messageID, () -> message);
        log.info(String.format("Attempting to add message with id '%s' to server", messageID));
        if (hasUUID(messageID)) {
            SameIDException exception = new SameIDException(messageID);
            log.error(String.format("Failed to add message with id '%s' to server because: %s", messageID, exception));
            throw exception;
        }
        log.info(String.format("Adding message with id '%s' to server", messageID));
        putMessage(messageID, message);
        log.traceExit();
    }

    public void deleteMessageFromServer(final String messageID) throws NoIDException {
        log.traceEntry(() -> messageID);
        log.info(String.format("Attempting to delete message with id '%s' from server", messageID));
        if (!hasUUID(messageID)) {
            NoIDException exception = new NoIDException(messageID);
            log.error(String.format("Failed to delete message with id '%s' from server because: %s", messageID, exception));
            throw exception;
        }
        log.info(String.format("Deleting message with id '%s' from server", messageID));
        removeMessage(messageID);
        log.traceExit();
    }

    public JsonObject getMessage(final String uuid) {
        log.traceEntry(() -> uuid);
        log.info(String.format("Getting message with id '%s' from Message Store", uuid));
        if (messages.containsKey(uuid)) {
            return log.traceExit(messages.get(uuid));
        }
        log.traceExit();
        return null;
    }

    public void putMessage(final String uuid, final JsonObject message) {
        log.traceEntry(() -> uuid, () -> message);
        log.info(String.format("Putting message with id '%s' in Message Store", uuid));
        messages.put(uuid, message);
        log.traceExit();
    }

    public void removeMessage(final String uuid) {
        log.traceEntry(() -> uuid);

        log.info(String.format("Attempting to remove message with id '%s' from Message Store", uuid));
        if (hasUUID(uuid)) {
            log.info(String.format("Removing message with id '%s' from server", uuid));
            messages.remove(uuid);
        } else {
            log.info(String.format("Could not find with id '%s' to remove from server", uuid));
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

    public Map<String, JsonObject> getMessages() {
        log.traceEntry();
        return log.traceExit(messages);
    }
}
