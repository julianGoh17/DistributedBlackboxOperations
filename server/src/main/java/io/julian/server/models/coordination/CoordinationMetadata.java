package io.julian.server.models.coordination;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.julian.server.models.HTTPRequest;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class CoordinationMetadata {
    private static final Logger log = LogManager.getLogger(CoordinationMetadata.class.getName());
    private final HTTPRequest request;
    private final String messageID;
    private final CoordinationTimestamp timestamp;
    private final String type;

    public static final String TIMESTAMP_KEY = "timestamp";
    public static final String REQUEST_KEY = "request";
    public static final String MESSAGE_ID_KEY = "messageId";
    public static final String TYPE_KEY = "type";

    public CoordinationMetadata(@JsonProperty(TIMESTAMP_KEY) final CoordinationTimestamp timestamp,
                                @JsonProperty(REQUEST_KEY) final HTTPRequest request,
                                @JsonProperty(MESSAGE_ID_KEY) final String messageID,
                                @JsonProperty(TYPE_KEY) final String type) {
        this.timestamp = timestamp;
        this.request = request;
        this.messageID = messageID;
        this.type = type;
    }

    public CoordinationMetadata(final HTTPRequest request) {
        this.timestamp = new CoordinationTimestamp();
        this.request = request;
        this.messageID = null;
        this.type = null;
    }

    public CoordinationMetadata(final HTTPRequest request, final String type) {
        this.timestamp = new CoordinationTimestamp();
        this.request = request;
        this.messageID = null;
        this.type = type;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(TIMESTAMP_KEY, this.timestamp.toValue())
            .put(REQUEST_KEY, this.request.toValue())
            .put(MESSAGE_ID_KEY, this.messageID)
            .put(TYPE_KEY, this.type));
    }
}
