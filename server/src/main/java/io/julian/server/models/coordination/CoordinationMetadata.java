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
    private final String fromServerId;
    private final HTTPRequest request;
    private final String originalID;
    private final String newID;
    private final CoordinationTimestamp timestamp;

    public static final String FROM_SERVER_ID_KEY = "fromServerId";
    public static final String TIMESTAMP_KEY = "timestamp";
    public static final String REQUEST_KEY = "request";
    public static final String ORIGINAL_ID_KEY = "originalId";
    public static final String NEW_ID_KEY = "newId";

    public CoordinationMetadata(@JsonProperty(FROM_SERVER_ID_KEY) final String fromServerId,
                                @JsonProperty(TIMESTAMP_KEY) final CoordinationTimestamp timestamp,
                                @JsonProperty(REQUEST_KEY) final HTTPRequest request,
                                @JsonProperty(ORIGINAL_ID_KEY) final String originalID,
                                @JsonProperty(NEW_ID_KEY) final String newID) {
        this.fromServerId = fromServerId;
        this.timestamp = timestamp;
        this.request = request;
        this.originalID = originalID;
        this.newID = newID;
    }

    public CoordinationMetadata(final String fromServerId, final HTTPRequest request) {
        this.fromServerId = fromServerId;
        this.timestamp = new CoordinationTimestamp();
        this.request = request;
        this.originalID = null;
        this.newID = null;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(FROM_SERVER_ID_KEY, this.fromServerId)
            .put(TIMESTAMP_KEY, this.timestamp.toValue())
            .put(REQUEST_KEY, this.request.toValue())
            .put(ORIGINAL_ID_KEY, this.originalID)
            .put(NEW_ID_KEY, this.newID));
    }
}
