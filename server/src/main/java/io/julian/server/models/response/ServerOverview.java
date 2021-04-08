package io.julian.server.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerOverview {
    private static final Logger log = LogManager.getLogger(ServerOverview.class);
    private final String host;
    private final int port;
    private final int numMessages;

    public final static String HOST_KEY = "host";
    public final static String PORT_KEY = "port";
    public final static String NUM_MESSAGES_KEY = "numMessages";

    public ServerOverview(@JsonProperty(HOST_KEY) final String host,
                          @JsonProperty(PORT_KEY) final int port,
                          @JsonProperty(NUM_MESSAGES_KEY) final int numMessages) {
        this.host = host;
        this.port = port;
        this.numMessages = numMessages;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(HOST_KEY, host)
            .put(PORT_KEY, port)
            .put(NUM_MESSAGES_KEY, numMessages));
    }

    public String getHost() {
        log.traceEntry();
        return log.traceExit(host);
    }

    public int getPort() {
        log.traceEntry();
        return log.traceExit(port);
    }

    public int getNumMessages() {
        log.traceEntry();
        return log.traceExit(numMessages);
    }
}
