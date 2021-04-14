package io.julian.gossip.components;

import io.julian.gossip.models.MessageUpdate;
import io.julian.server.components.MessageStore;
import io.vertx.core.json.JsonArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class State {
    private final static Logger log = LogManager.getLogger(State.class);
    private final MessageStore messages;

    public State(final MessageStore messages) {
        this.messages = messages;
    }

    public MessageStore getMessages() {
        log.traceEntry();
        return log.traceExit(messages);
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
