package io.julian.server.client;

import io.julian.server.models.control.OtherServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegistryManager {
    private final Logger log = LogManager.getLogger(RegistryManager.class.getName());
    private final List<OtherServerConfiguration> otherServers = new ArrayList<>();

    public List<OtherServerConfiguration> getOtherServers() {
        log.traceEntry();
        return log.traceExit(otherServers);
    }

    public List<OtherServerConfiguration> getOtherServersWithLabel(final String label) {
        log.traceEntry(() -> label);
        return log.traceExit(otherServers.stream()
            .filter(server -> label.equals(server.getLabel()))
            .collect(Collectors.toList()));
    }

    public void registerServer(final String host, final int port) {
        log.traceEntry(() -> host, () -> port);
        otherServers.add(new OtherServerConfiguration(host, port));
        log.traceExit();
    }

    public void registerServerWithLabel(final String host, final int port, final String label) {
        log.traceEntry(() -> host, () -> port, () -> label);
        otherServers.add(new OtherServerConfiguration(host, port, label));
        log.traceExit();
    }
}
