package io.julian.metrics.collector.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class SuccessResponse {
    private final static Logger log = LogManager.getLogger(SuccessResponse.class);
    private final int statusCode;

    public final static String STATUS_CODE_KEY = "statusCode";

    public SuccessResponse(@JsonProperty(STATUS_CODE_KEY) final int statusCode) {
        this.statusCode = statusCode;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject().put(STATUS_CODE_KEY, statusCode));
    }
}
