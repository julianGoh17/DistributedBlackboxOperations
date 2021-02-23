package io.julian.server.models.control;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class ServerConfiguration {
    private final Logger log = LogManager.getLogger(ServerConfiguration.class.getName());
    private String host;
    private int port;
    private String label;

    public ServerConfiguration(final String host, final int port, final String label) {
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
}
