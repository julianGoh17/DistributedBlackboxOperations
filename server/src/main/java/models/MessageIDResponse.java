package models;

import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageIDResponse {
    private final static Logger logger = LogManager.getLogger(MessageIDResponse.class.getName());
    private final String uuid;
    public static final String messageIdKey = "messageId";

    public MessageIDResponse(String uuid) {
        this.uuid = uuid;
    }

    public JsonObject toJson() {
        logger.traceEntry();
        return logger.traceExit(new JsonObject()
                .put(messageIdKey, this.uuid));
    }
}
