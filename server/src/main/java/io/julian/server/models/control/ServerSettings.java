package io.julian.server.models.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.julian.server.models.ServerStatus;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class ServerSettings {
    private final Logger log = LogManager.getLogger(ServerStatus.class.getName());

    public static final String FAILURE_CHANCE_KEY = "failureChance";
    public static final String STATUS_KEY = "status";

    private final ServerStatus status;
    private final Float failureChance;

    public ServerSettings(@JsonProperty(STATUS_KEY) final ServerStatus status,
                          @JsonProperty(FAILURE_CHANCE_KEY) final Float failureChance) {
        this.status = status;
        this.failureChance = failureChance;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(STATUS_KEY, status.toValue())
            .put(FAILURE_CHANCE_KEY, failureChance));
    }
}
