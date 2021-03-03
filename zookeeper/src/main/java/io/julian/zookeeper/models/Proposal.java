package io.julian.zookeeper.models;

import io.julian.server.models.control.ClientMessage;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class Proposal {
    private final static Logger log = LogManager.getLogger(Proposal.class.getName());
    public final static String NEW_STATE_KEY = "new_state";
    public final static String TRANSACTION_ID_KEY = "transaction_id";

    private final ClientMessage newState;
    private final Zxid transactionId;

    public Proposal(final ClientMessage newState, final Zxid transactionId) {
        this.newState = newState;
        this.transactionId = transactionId;
    }

    public static Proposal mapFrom(final JsonObject json) {
        log.traceEntry(() -> json);

        return log.traceExit(
            new Proposal(
                ClientMessage.fromJson(json.getJsonObject(NEW_STATE_KEY)),
                json.getJsonObject(TRANSACTION_ID_KEY).mapTo(Zxid.class)));
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(NEW_STATE_KEY, newState.toJson())
            .put(TRANSACTION_ID_KEY, transactionId.toJson()));
    }
}
