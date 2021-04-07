package io.julian.metrics.collector.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class ErrorResponse {
    private final static Logger log = LogManager.getLogger(SuccessResponse.class);
    private final int statusCode;
    private final Throwable error;

    public final static String STATUS_CODE_KEY = "statusCode";
    public final static String ERROR_KEY = "error";

    public ErrorResponse(@JsonProperty(STATUS_CODE_KEY) final int statusCode,
                         @JsonProperty(ERROR_KEY) final Throwable error) {
        this.statusCode = statusCode;
        this.error = error;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(STATUS_CODE_KEY, statusCode)
            .put(ERROR_KEY, error.getMessage()));
    }
}
