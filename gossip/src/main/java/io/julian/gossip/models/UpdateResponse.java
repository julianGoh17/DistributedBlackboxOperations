package io.julian.gossip.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    public final static String HAS_PROCESSED_ID_KEY = "hasProcessedId";

    private String messageId;
    private Boolean hasProcessedId;

    public UpdateResponse(@JsonProperty(MESSAGE_ID_KEY) final String messageId,
                          @JsonProperty(HAS_PROCESSED_ID_KEY) final Boolean hasProcessedId) {
        this.messageId = messageId;
        this.hasProcessedId = hasProcessedId;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(MESSAGE_ID_KEY, messageId)
            .put(HAS_PROCESSED_ID_KEY, hasProcessedId));
    }
}
