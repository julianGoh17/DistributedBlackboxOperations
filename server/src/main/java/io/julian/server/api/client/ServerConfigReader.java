package io.julian.server.api.client;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ServerConfigReader {
    private final static Logger log = LogManager.getLogger(ServerConfigReader.class);
    private final static String SERVER_DELIMITER = "\n";
    private final static String PORT_DELIMITER = ":";
    private final static int HOST_INDEX = 0;
    private final static int PORT_INDEX = 1;

    public ArrayList<Pair<String, Integer>> getServerConfigurations(final String filePath) throws IOException, NumberFormatException {
        log.traceEntry(() -> filePath);
        String contents = readFile(filePath);
        ArrayList<Pair<String, Integer>> serverConfigurations = new ArrayList<>();
        for (String servers : contents.split(SERVER_DELIMITER)) {
            if (servers.length() > 0) {
                String[] components = servers.split(PORT_DELIMITER);
                if (components.length == 2) {
                    String host = components[HOST_INDEX];
                    int port = Integer.parseInt(components[PORT_INDEX]);
                    log.info(String.format("Registering server '%s:%d'", host, port));
                    serverConfigurations.add(Pair.of(host, port));
                }
            }
        }
        return log.traceExit(serverConfigurations);
    }

    /*
     * Exposed for Testing
     */
    public String readFile(final String filePath) throws IOException {
        log.traceEntry(() -> filePath);
        Path path = Path.of(filePath);
        return log.traceExit(Files.readString(path));
    }
}
