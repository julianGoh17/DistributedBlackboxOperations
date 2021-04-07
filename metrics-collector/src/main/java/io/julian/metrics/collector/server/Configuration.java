package io.julian.metrics.collector.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class Configuration {
    public static final Logger log = LogManager.getLogger(Configuration.class.getName());

    public static final String OPENAPI_SPEC_LOCATION_ENV = "OPENAPI_SPEC_LOCATION";
    public static final String DEFAULT_OPENAPI_SPEC_LOCATION = "src/main/resources/metrics-collector-endpoints.yaml";
    private final String openApiSpecLocation;

    public static final String REPORT_FILE_PATH_ENV = "REPORT_FILE_PATH";
    public static final String DEFAULT_REPORT_FILE_PATH = String.format("%s/client/src/main/resources/generated/report", System.getProperty("user.dir"));
    private String reportPath;

    public Configuration() {
        openApiSpecLocation = getOrDefault(OPENAPI_SPEC_LOCATION_ENV, DEFAULT_OPENAPI_SPEC_LOCATION);
        reportPath = getOrDefault(REPORT_FILE_PATH_ENV, DEFAULT_REPORT_FILE_PATH);
    }

    public String getOpenApiSpecLocation() {
        log.traceEntry();
        return log.traceExit(openApiSpecLocation);
    }

    public String getReportPath() {
        log.traceEntry();
        return log.traceExit(reportPath);
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
}
