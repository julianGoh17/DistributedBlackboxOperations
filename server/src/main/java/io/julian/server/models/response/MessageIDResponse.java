package io.julian.server.models.response;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class MessageIDResponse {
    private final static Logger log = LogManager.getLogger(MessageIDResponse.class.getName());
    private final String uuid;
    public static final String MESSAGE_ID_KEY = "messageId";

    public MessageIDResponse(final String uuid) {
        this.uuid = uuid;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(MESSAGE_ID_KEY, this.uuid));
    }
}
