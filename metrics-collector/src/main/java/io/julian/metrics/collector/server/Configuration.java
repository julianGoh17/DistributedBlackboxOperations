package io.julian.metrics.collector.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class Configuration {
    public static final Logger log = LogManager.getLogger(Configuration.class.getName());

    public static final String SERVER_HOST_ENV = "SERVER_HOST";
    public static final String DEFAULT_SERVER_HOST = "localhost";
    private final String serverHost;

    public static final String SERVER_PORT_ENV = "SERVER_PORT";
    public static final int DEFAULT_SERVER_PORT = 9090;
    private final int serverPort;

    public static final String OPENAPI_SPEC_LOCATION_ENV = "OPENAPI_SPEC_LOCATION";
    public static final String DEFAULT_OPENAPI_SPEC_LOCATION = "src/main/resources/metrics-collector-endpoints.yaml";
    private final String openApiSpecLocation;

    public static final String REPORT_FILE_PATH_ENV = "REPORT_FILE_PATH";
    public static final String DEFAULT_REPORT_FILE_PATH = String.format("%s/client/src/main/resources/generated/report", System.getProperty("user.dir"));
    private String reportPath;

    public Configuration() {
        openApiSpecLocation = getOrDefault(OPENAPI_SPEC_LOCATION_ENV, DEFAULT_OPENAPI_SPEC_LOCATION);
        reportPath = getOrDefault(REPORT_FILE_PATH_ENV, DEFAULT_REPORT_FILE_PATH);
        serverHost = getOrDefault(SERVER_HOST_ENV, DEFAULT_SERVER_HOST);
        serverPort = getOrDefault(SERVER_PORT_ENV, DEFAULT_SERVER_PORT);
    }

    public String getOpenApiSpecLocation() {
        log.traceEntry();
        return log.traceExit(openApiSpecLocation);
    }

    public String getReportPath() {
        log.traceEntry();
        return log.traceExit(reportPath);
    }

    public String getServerHost() {
        log.traceEntry();
        return log.traceExit(serverHost);
    }

    public int getServerPort() {
        log.traceEntry();
        return log.traceExit(serverPort);
    }

    public void setReportPath(final String reportPath) {
        log.traceEntry(() -> reportPath);
        this.reportPath = reportPath;
        log.traceExit();
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
