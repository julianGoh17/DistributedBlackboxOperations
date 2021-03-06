package io.julian.server.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.julian.server.endpoints.control.SetServerSettingsHandler;
import io.julian.server.models.ServerStatus;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class SetStatusResponse {
    private final static Logger log = LogManager.getLogger(SetServerSettingsHandler.class.getName());

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
