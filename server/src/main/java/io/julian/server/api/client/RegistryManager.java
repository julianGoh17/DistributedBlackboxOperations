package io.julian.server.api.client;

import io.julian.server.components.Configuration;
import io.julian.server.models.control.ServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegistryManager {
    private final Logger log = LogManager.getLogger(RegistryManager.class.getName());
    private final List<ServerConfiguration> otherServers = new ArrayList<>();
    private final Configuration configuration = new Configuration();

    public List<ServerConfiguration> getOtherServers() {
        log.traceEntry();
        return log.traceExit(otherServers);
    }

    public List<ServerConfiguration> getOtherServersWithLabel(final String label) {
        log.traceEntry(() -> label);
        log.info(String.format("Retrieving servers with label '%s'", label));
        return log.traceExit(otherServers.stream()
            .filter(server -> label.equals(server.getLabel()))
            .collect(Collectors.toList()));
    }

    public void registerServer(final String host, final int port) {
        log.traceEntry(() -> host, () -> port);
        log.info(String.format("Registering server '%s:%d'", host, port));
        otherServers.add(new ServerConfiguration(host, port));
        log.traceExit();
    }

    public void registerServerWithLabel(final String host, final int port, final String label) {
        log.traceEntry(() -> host, () -> port, () -> label);
        log.info(String.format("Registering server '%s:%d' with label '%s'", host, port, label));
        otherServers.add(new ServerConfiguration(host, port, label));
        log.traceExit();
    }

    /**
     * Exposed for Testing
     */
    public void registerServersInsideServerConfiguration() {
        log.traceEntry();
        if (!configuration.getOpenapiSpecLocation().equals(Configuration.DEFAULT_SERVER_CONFIGURATION_LOCATION)) {
            log.info(String.format("Registering servers from file in path '%s'", configuration.getServerConfigurationLocation()));

        } else {
            log.info("Skipping registering servers as no file passed in");
        }
        log.traceExit();
    }

    private String readFile(final String filePath) throws IOException {
        log.traceEntry(() -> filePath);
        Path path = Path.of(filePath);
        return log.traceExit(Files.readString(path));
    }
}
