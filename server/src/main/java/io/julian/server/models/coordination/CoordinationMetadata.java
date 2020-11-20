package io.julian.server.models.coordination;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private final CoordinationTimestamp timestamp;

    public static final String FROM_SERVER_ID_KEY = "fromServerId";
    public static final String TIMESTAMP_KEY = "timestamp";

    public CoordinationMetadata(@JsonProperty("fromServerId") final String fromServerId,
                                @JsonProperty("timestamp") final CoordinationTimestamp timestamp) {
        this.fromServerId = fromServerId;
        this.timestamp = timestamp;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(FROM_SERVER_ID_KEY, this.fromServerId)
            .put(TIMESTAMP_KEY, this.timestamp.toValue()));
    }
}
