package io.julian.zookeeper.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class ShortenedExchange {
    private final static Logger log = LogManager.getLogger(ShortenedExchange.class.getName());
    private MessagePhase phase;
    private Zxid transactionID;

    public final static String PHASE_KEY = "phase";
    public final static String TRANSACTIONAL_ID_KEY = "transactional_id";

    public ShortenedExchange(@JsonProperty(PHASE_KEY) final MessagePhase phase,
                             @JsonProperty(TRANSACTIONAL_ID_KEY) final Zxid transactionID) {
        this.phase = phase;
        this.transactionID = transactionID;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(PHASE_KEY, phase.toValue())
            .put(TRANSACTIONAL_ID_KEY, transactionID.toJson()));
    }
}