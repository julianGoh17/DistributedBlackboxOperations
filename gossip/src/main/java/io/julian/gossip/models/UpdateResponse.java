package io.julian.gossip.models;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class UpdateResponse {
    private final static Logger log = LogManager.getLogger(UpdateResponse.class);
    public final static String MESSAGE_ID_KEY = "messageId";
    public final static String DOES_CONTAIN_ID_KEY = "doesContainId";

    private String messageId;
    private Boolean doesContainId;

    public UpdateResponse(final String messageId, final Boolean doesContainId) {
        this.messageId = messageId;
        this.doesContainId = doesContainId;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(MESSAGE_ID_KEY, messageId)
            .put(DOES_CONTAIN_ID_KEY, doesContainId));
    }
}
