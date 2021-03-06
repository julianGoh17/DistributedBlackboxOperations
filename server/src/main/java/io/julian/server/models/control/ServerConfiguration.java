package io.julian.server.models.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

@Getter
@Setter
public class ServerConfiguration {
    private final Logger log = LogManager.getLogger(ServerConfiguration.class.getName());
    private String host;
    private int port;
    private String label;

    public final static String HOST_KEY = "host";
    public final static String PORT_KEY = "port";
    public final static String LABEL_KEY = "label";

    @JsonCreator
    public ServerConfiguration(@JsonProperty(HOST_KEY) final String host,
                               @JsonProperty(PORT_KEY) final int port,
                               @JsonProperty(LABEL_KEY) final String label) {
        this.host = host;
        this.port = port;
        this.label = label;
    }

    public ServerConfiguration(final String host, final int port) {
        this.host = host;
        this.port = port;
        this.label = null;
    }

    public boolean isLabeled() {
        log.traceEntry();
        return log.traceExit(this.label != null);
    }

    @Override
    public String toString() {
        log.traceEntry();
        return log.traceExit(isLabeled() ?
            String.format("'%s:%d with no label'", host, port) :
            String.format("'%s:%d' with label '%s'", host, port, label));
    }

    public boolean isEqual(final ServerConfiguration otherServer) {
        log.traceEntry(() -> otherServer);

        // Strings can be null, this prevents NPE in later check
        String filteredCurrentLabel = Optional.ofNullable(label).orElse("");
        String filteredOtherLabel = Optional.ofNullable(otherServer.getLabel()).orElse("");

        return log.traceExit(isHostAndPortEqual(otherServer) &&
            filteredCurrentLabel.equals(filteredOtherLabel));
    }

    public boolean isHostAndPortEqual(final ServerConfiguration otherServer) {
        log.traceEntry(() -> otherServer);
        return log.traceExit(otherServer.getHost().equals(host) &&
            otherServer.getPort() == port);
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject().put(HOST_KEY, host)
            .put(PORT_KEY, port)
            .put(LABEL_KEY, label));
    }
}
