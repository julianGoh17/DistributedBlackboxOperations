package io.julian.server.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class LabelResponse {
    private final static Logger log = LogManager.getLogger(LabelResponse.class.getName());

    public final static String LABEL_KEY = "label";
    private final String label;

    public LabelResponse(@JsonProperty(LABEL_KEY) final String label) {
        this.label = label;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject().put(LABEL_KEY, this.label));
    }
}
