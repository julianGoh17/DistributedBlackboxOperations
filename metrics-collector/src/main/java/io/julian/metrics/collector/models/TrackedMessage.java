package io.julian.metrics.collector.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class TrackedMessage {
    private final static Logger log = LogManager.getLogger(SuccessResponse.class);
    private final int statusCode;
    private final String messageId;
    private final float messageSize;

    public final static String STATUS_CODE_KEY = "statusCode";
    public final static String MESSAGE_ID_KEY = "messageId";
    public final static String MESSAGE_SIZE_KEY = "messageSize";

    public TrackedMessage(@JsonProperty(STATUS_CODE_KEY) final int statusCode,
                         @JsonProperty(MESSAGE_ID_KEY) final String messageId,
                          @JsonProperty(MESSAGE_SIZE_KEY) final float messageSize) {
        this.statusCode = statusCode;
        this.messageId = messageId;
        this.messageSize = messageSize;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(STATUS_CODE_KEY, statusCode)
            .put(MESSAGE_ID_KEY, messageId)
            .put(MESSAGE_SIZE_KEY, messageSize));
    }
}
