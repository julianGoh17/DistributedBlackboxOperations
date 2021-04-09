package io.julian.client.operations;

import io.julian.server.models.control.ServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ServerFileReader {
    private final static Logger log = LogManager.getLogger(ServerFileReader.class);

    public static List<ServerConfiguration> readServerFile(final String serverFilePath) throws FileNotFoundException {
        log.traceEntry(() -> serverFilePath);
        log.info(String.format("Reading server configurations from '%s'", serverFilePath));
        File file = new File(serverFilePath);
        Scanner scanner = new Scanner(file);
        ArrayList<ServerConfiguration> configurations = new ArrayList<>();
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (line.length() > 0) {
                ServerConfiguration configuration = createServerConfiguration(line);
                if (configuration != null) {
                    configurations.add(createServerConfiguration(line));
                } else {
                    log.info(String.format("Couldn't add '%s' to server list", line));
                }
            }
        }
        return log.traceExit(configurations);
    }

    public static ServerConfiguration createServerConfiguration(final String line) {
        log.traceEntry(() -> line);
        String[] components = line.split(":");
        if (components.length != 2) {
            log.traceExit();
            return null;
        }

        try {
            int port = Integer.parseInt(components[1]);
            return log.traceExit(new ServerConfiguration(components[0], port));
        } catch (final NumberFormatException e) {
            log.info(String.format("Improper format of '%s'", line));
            log.traceExit();
            return null;
        }
    }


}
