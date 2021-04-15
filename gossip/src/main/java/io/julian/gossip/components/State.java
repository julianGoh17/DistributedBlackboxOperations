package io.julian.gossip.components;

import io.julian.gossip.models.MessageUpdate;
import io.julian.server.components.MessageStore;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.json.JsonArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

public class State {
    private final static Logger log = LogManager.getLogger(State.class);
    private final MessageStore messages;
    private final ConcurrentLinkedQueue<CoordinationMessage> deadLetters;

    public State(final MessageStore messages, final ConcurrentLinkedQueue<CoordinationMessage> deadLetters) {
        this.messages = messages;
        this.deadLetters = deadLetters;
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
}
