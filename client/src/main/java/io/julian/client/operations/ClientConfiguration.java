package io.julian.client.operations;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

@Setter
public class ClientConfiguration {
    private final static Logger log = LogManager.getLogger(ClientConfiguration.class.getName());
    public final static String MESSAGE_FILE_PATH_ENV = "MESSAGE_FILE_PATH";
    public final static String DEFAULT_MESSAGE_FILE_PATH = String.format("%s/client/src/main/resources/generated/messages", System.getProperty("user.dir"));
    private final String messagePath;

    public final static String OPERATIONS_FILE_PATH_ENV = "OPERATIONS_FILE_PATH";
    public final static String DEFAULT_OPERATIONS_MESSAGE_FILE_PATH = String.format("%s/client/src/main/resources/generated/operations", System.getProperty("user.dir"));
    private final String operationsFilePath;

    public final static String REPORT_FILE_PATH_ENV = "REPORT_FILE_PATH";
    public final static String DEFAULT_REPORT_MESSAGE_FILE_PATH = String.format("%s/client/src/main/resources/generated/report", System.getProperty("user.dir"));
    private final String reportFilePath;

    public final static String SERVER_HOSTS_FILE_PATH_ENV = "SERVER_HOSTS_FILE_PATH";
    public final static String DEFAULT_SERVER_HOSTS_FILE_PATH_ENV = String.format("%s/../test-run/zookeepers/settings/server-list.txt", System.getProperty("user.dir"));
    private String serverHostsFilePath;

    public final static String SERVER_HOST_ENV = "SERVER_HOST";
    public final static String DEFAULT_SERVER_HOST = io.julian.server.components.Configuration.DEFAULT_SERVER_HOST;
    private final String host;

    public static final String SERVER_PORT_ENV = "SERVER_PORT";
    public static final int DEFAULT_SERVER_PORT = io.julian.server.components.Configuration.DEFAULT_SERVER_PORT;
    private final int port;

    public ClientConfiguration() {
        this.messagePath = getOrDefault(MESSAGE_FILE_PATH_ENV, DEFAULT_MESSAGE_FILE_PATH);
        this.operationsFilePath = getOrDefault(OPERATIONS_FILE_PATH_ENV, DEFAULT_OPERATIONS_MESSAGE_FILE_PATH);
        this.reportFilePath = getOrDefault(REPORT_FILE_PATH_ENV, DEFAULT_REPORT_MESSAGE_FILE_PATH);
        this.serverHostsFilePath = getOrDefault(SERVER_HOSTS_FILE_PATH_ENV, DEFAULT_SERVER_HOSTS_FILE_PATH_ENV);
        this.host = getOrDefault(SERVER_HOST_ENV, DEFAULT_SERVER_HOST);
        this.port = getOrDefault(SERVER_PORT_ENV, DEFAULT_SERVER_PORT);
    }

    public String getMessageFilePath() {
        log.traceEntry();
        return log.traceExit(messagePath);
    }

    public String getOperationsFilePath() {
        log.traceEntry();
        return log.traceExit(operationsFilePath);
    }

    public String getReportFilePath() {
        log.traceEntry();
        return log.traceExit(this.reportFilePath);
    }

    public String getServerHostsFilePath() {
        log.traceEntry();
        return log.traceExit(this.serverHostsFilePath);
    }

    public String getServerHost() {
        log.traceEntry();
        return log.traceExit(host);
    }

    public int getServerPort() {
        log.traceEntry();
        return log.traceExit(port);
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
