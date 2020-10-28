package io.julian.client.operations;

import io.julian.server.components.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Optional;

public class Configuration {
    private static final Logger log = LogManager.getLogger(Configuration.class.getName());
    public static final String MESSAGE_FILE_PATH_ENV = "MESSAGE_FILE_PATH";
    public static final String DEFAULT_MESSAGE_FILE_PATH = "/Users/juliangoh/Projects/src/java/DistributedBlackboxOperations/client/src/main/resources/generated/messages";

    public static final String OPERATIONS_FILE_PATH_ENV = "OPERATIONS_FILE_PATH";
    public static final String OPERATIONS_MESSAGE_FILE_PATH = File.separator + "blah";

    public static final String SERVER_HOST_ENV = "SERVER_HOST";
    public static final String DEFAULT_SERVER_HOST = Server.DEFAULT_HOST;

    public static final String SERVER_PORT_ENV = "SERVER_PORT";
    public static final int DEFAULT_SERVER_PORT = Server.DEFAULT_SERVER_PORT;

    public static String getMessageFilePath() {
        log.traceEntry();
        return log.traceExit(getOrDefault(MESSAGE_FILE_PATH_ENV, DEFAULT_MESSAGE_FILE_PATH));
    }

    public static String getOperationsFilePath() {
        log.traceEntry();
        return log.traceExit(getOrDefault(OPERATIONS_FILE_PATH_ENV, OPERATIONS_MESSAGE_FILE_PATH));
    }

    public static String getServerHost() {
        log.traceEntry();
        return log.traceExit(getOrDefault(SERVER_HOST_ENV, DEFAULT_SERVER_HOST));
    }

    public static Integer getServerPort() {
        log.traceEntry();
        return log.traceExit(getOrDefault(SERVER_PORT_ENV, DEFAULT_SERVER_PORT));
    }

    public static String getOrDefault(final String key, final String defaultVal) {
        log.traceEntry(() -> key, () -> defaultVal);
        return log.traceExit(Optional.ofNullable(System.getenv(key))
            .orElse(defaultVal));
    }

    public static int getOrDefault(final String key, final int defaultVal) {
        log.traceEntry(() -> key, () -> defaultVal);
        try {
            return log.traceExit(Integer.parseInt(System.getenv(key)));
        } catch (NumberFormatException e) {
            return log.traceExit(defaultVal);
        }
    }
}
