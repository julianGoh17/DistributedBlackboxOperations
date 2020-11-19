package io.julian.server.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.julian.server.endpoints.SetStatusHandler;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class SetStatusResponse {
    private final static Logger log = LogManager.getLogger(SetStatusHandler.class.getName());

    private final ServerStatus status;
    public static final String STATUS_KEY = "status";

    public SetStatusResponse(@JsonProperty(STATUS_KEY) final ServerStatus status) {
        this.status = status;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject().put(STATUS_KEY, status.toValue()));
    }
}
