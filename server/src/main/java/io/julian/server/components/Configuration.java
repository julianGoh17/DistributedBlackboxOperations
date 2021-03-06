package io.julian.server.components;

import io.julian.server.models.DistributedAlgorithmSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class Configuration {
    public static final Logger log = LogManager.getLogger(Configuration.class.getName());

    public static final String OPENAPI_SPEC_LOCATION_ENV = "OPENAPI_SPEC_LOCATION";
    public static final String DEFAULT_OPENAPI_SPEC_LOCATION = "src/main/resources/ServerEndpoints.yaml";
    private String openApiSpecLocation;

    public static final String SERVER_CONFIGURATION_LOCATION = "SERVER_CONFIGURATION_LOCATION";
    public static final String DEFAULT_SERVER_CONFIGURATION_LOCATION = "";
    private String serverConfigurationLocation;

    public static final String SERVER_HOST_ENV = "SERVER_HOST";
    public static final String DEFAULT_SERVER_HOST = "localhost";
    private String host;

    public static final String SERVER_PORT_ENV = "SERVER_PORT";
    public static final int DEFAULT_SERVER_PORT = 8888;
    private int port;

    public static final String METRICS_COLLECTOR_HOST_ENV = "METRICS_COLLECTOR_HOST";
    public static final String DEFAULT_METRICS_COLLECTOR_HOST = "localhost";
    private String metricsCollectorHost;

    public static final String METRICS_COLLECTOR_PORT_ENV = "METRICS_COLLECTOR_PORT";
    public static final int DEFAULT_METRICS_COLLECTOR_PORT = 9090;
    private int metricsCollectorPort;

    public static final String DOES_PROCESS_REQUEST_ENV = "DOES_PROCESS_REQUEST";
    public static final boolean DEFAULT_DOES_PROCESS_REQUEST = true;
    private boolean doesProcessRequest;

    public static final String JAR_FILE_PATH_ENV = "JAR_FILE_PATH";
    private String jarFilePath;
    public static final String PACKAGE_NAME_ENV = "PACKAGE_NAME";
    private String packageName;

    public Configuration() {
        this.openApiSpecLocation = getOrDefault(OPENAPI_SPEC_LOCATION_ENV, DEFAULT_OPENAPI_SPEC_LOCATION);
        this.serverConfigurationLocation = getOrDefault(SERVER_CONFIGURATION_LOCATION, DEFAULT_SERVER_CONFIGURATION_LOCATION);
        this.host = getOrDefault(SERVER_HOST_ENV, DEFAULT_SERVER_HOST);
        this.port = getOrDefault(SERVER_PORT_ENV, DEFAULT_SERVER_PORT);
        this.metricsCollectorHost = getOrDefault(METRICS_COLLECTOR_HOST_ENV, DEFAULT_METRICS_COLLECTOR_HOST);
        this.metricsCollectorPort = getOrDefault(METRICS_COLLECTOR_PORT_ENV, DEFAULT_METRICS_COLLECTOR_PORT);
        this.jarFilePath = getOrDefault(JAR_FILE_PATH_ENV, "");
        this.packageName = getOrDefault(PACKAGE_NAME_ENV, "");
        this.doesProcessRequest = getOrDefault(DOES_PROCESS_REQUEST_ENV, DEFAULT_DOES_PROCESS_REQUEST);
    }

    public int getServerPort() {
        log.traceEntry();
        return log.traceExit(port);
    }

    public void setServerPort(final int port) {
        log.traceEntry(() -> port);
        this.port = port;
        log.traceExit();
    }

    public String getServerHost() {
        log.traceEntry();
        return log.traceExit(host);
    }

    public void setServerHost(final String host) {
        log.traceEntry(() -> host);
        this.host = host;
        log.traceExit();
    }

    public int getMetricsCollectorPort() {
        log.traceEntry();
        return log.traceExit(metricsCollectorPort);
    }

    public void setMetricsCollectorPort(final int metricsCollectorPort) {
        log.traceEntry(() -> metricsCollectorPort);
        this.metricsCollectorPort = metricsCollectorPort;
        log.traceExit();
    }

    public String getMetricsCollectorHost() {
        log.traceEntry();
        return log.traceExit(metricsCollectorHost);
    }

    public void setMetricsCollectorHost(final String metricsCollectorHost) {
        log.traceEntry(() -> metricsCollectorHost);
        this.metricsCollectorHost = metricsCollectorHost;
        log.traceExit();
    }

    public String getOpenapiSpecLocation() {
        log.traceEntry();
        return log.traceExit(openApiSpecLocation);
    }

    public void setOpenapiSpecLocation(final String openApiSpecLocation) {
        log.traceEntry(() -> openApiSpecLocation);
        this.openApiSpecLocation = openApiSpecLocation;
        log.traceExit();
    }

    public String getServerConfigurationLocation() {
        log.traceEntry();
        return log.traceExit(serverConfigurationLocation);
    }

    public void setServerConfigurationLocation(final String serverConfigurationLocation) {
        log.traceEntry(() -> serverConfigurationLocation);
        this.serverConfigurationLocation = serverConfigurationLocation;
        log.traceExit();
    }

    public String getJarFilePath() {
        log.traceEntry();
        return log.traceExit(jarFilePath);
    }

    public void setJarFilePath(final String jarFilePath) {
        log.traceEntry(() -> jarFilePath);
        this.jarFilePath = jarFilePath;
        log.traceExit();
    }

    public boolean isJarFilePathEnvInstantiated() {
        log.traceEntry();
        return log.traceExit(isVariableInstantiated(JAR_FILE_PATH_ENV));
    }

    public String getPackageName() {
        log.traceEntry();
        return log.traceExit(packageName);
    }

    public void setPackageName(final String packageName) {
        log.traceEntry(() -> packageName);
        this.packageName = packageName;
        log.traceExit();
    }

    public boolean doesProcessRequest() {
        log.traceEntry();
        return log.traceExit(doesProcessRequest);
    }

    public void setDoesProcessRequest(final boolean doesProcessRequest) {
        log.traceEntry(() -> doesProcessRequest);
        this.doesProcessRequest = doesProcessRequest;
        log.traceExit();
    }

    public boolean isPackageNameEnvInstantiated() {
        log.traceEntry();
        return log.traceExit(isVariableInstantiated(PACKAGE_NAME_ENV));
    }

    public DistributedAlgorithmSettings getDistributedAlgorithmSettings() {
        log.traceEntry();
        return log.traceExit(new DistributedAlgorithmSettings(isJarFilePathEnvInstantiated(),
            isPackageNameEnvInstantiated(),
            getJarFilePath(),
            getPackageName()));
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

    public static boolean getOrDefault(final String key, final boolean defaultVal) {
        log.traceEntry(() -> key, () -> defaultVal);
        return log.traceExit(Optional.ofNullable(System.getenv(key))
            .map(Boolean::parseBoolean)
            .orElse(DEFAULT_DOES_PROCESS_REQUEST));
    }

    public static boolean isVariableInstantiated(final String key) {
        log.traceEntry(() -> key);
        return log.traceExit(Optional.ofNullable(System.getenv(key)))
            .isPresent();
    }
}
