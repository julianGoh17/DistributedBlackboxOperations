package io.julian.client.model.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OverviewComparison {
    private final static Logger log = LogManager.getLogger(OverviewComparison.class);
    private final List<String> missingIdsInServer = new ArrayList<>();
    private final List<String> missingIdsInClient = new ArrayList<>();
    private final String host;
    private final int port;
    private final LocalDateTime timestamp;

    public OverviewComparison(final String host, final int port) {
        this.host = host;
        this.port = port;
        this.timestamp = LocalDateTime.now();
    }

    public void compareClientExpectedStateToServerOverview(final List<String> clientIds, final List<String> serverIds) {
        log.traceEntry(() -> clientIds, () -> serverIds);
        log.info("Comparing client expected ids and server ids");
        Set<String> client = new HashSet<>(clientIds);
        Set<String> server = new HashSet<>(serverIds);

        for (String clientId : clientIds) {
            if (!server.contains(clientId)) {
                missingIdsInServer.add(clientId);
            }
        }

        for (String serverId : serverIds) {
            if (!client.contains(serverId)) {
                missingIdsInClient.add(serverId);
            }
        }
        log.info("Finished comparing client expected ids and server ids");
        log.traceExit();
    }

    public List<String> getMissingIdsInServer() {
        log.traceEntry();
        return log.traceExit(missingIdsInServer);
    }

    public List<String> getMissingIdsInClient() {
        log.traceEntry();
        return log.traceExit(missingIdsInClient);
    }

    public String getHost() {
        log.traceEntry();
        return log.traceExit(host);
    }

    public int getPort() {
        log.traceEntry();
        return log.traceExit(port);
    }

    public LocalDateTime getTimestamp() {
        log.traceEntry();
        return log.traceExit(timestamp);
    }
}
