package io.julian.gossip.models;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class MessageUpdate {
    private final static Logger log = LogManager.getLogger(MessageUpdate.class);
    private String messageId;
    private JsonObject message;

    public final static String MESSAGE_ID_KEY = "messageId";
    public final static String MESSAGE_KEY = "message";

    public MessageUpdate(final String messageId, final JsonObject message) {
        this.messageId = messageId;
        this.message = message;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(MESSAGE_ID_KEY, messageId)
            .put(MESSAGE_KEY, message));
    }

    public static MessageUpdate fromJson(final JsonObject object) {
        log.traceEntry(() -> object);
        return log.traceExit(new MessageUpdate(object.getString(MESSAGE_ID_KEY), object.getJsonObject(MESSAGE_KEY)));
    }
}
