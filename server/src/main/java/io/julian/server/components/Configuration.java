package io.julian.server.components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class Configuration {
    public static final Logger log = LogManager.getLogger(Configuration.class.getName());

    public static final String OPENAPI_SPEC_LOCATION_ENV = "OPENAPI_SPEC_LOCATION";
    public static final String DEFAULT_OPENAPI_SPEC_LOCATION = "src/main/resources/ServerEndpoints.yaml";

    public static final String SERVER_HOST_ENV = "SERVER_HOST";
    public static final String DEFAULT_SERVER_HOST = "localhost";

    public static final String SERVER_PORT_ENV = "SERVER_PORT";
    public static final int DEFAULT_SERVER_PORT = 8888;

    public static int getServerPort() {
        log.traceEntry();
        return log.traceExit(getOrDefault(SERVER_PORT_ENV, DEFAULT_SERVER_PORT));
    }

    public static String getServerHost() {
        log.traceEntry();
        return log.traceExit(getOrDefault(SERVER_HOST_ENV, DEFAULT_SERVER_HOST));
    }

    public static String getOpenapiSpecLocation() {
        log.traceEntry();
        return log.traceExit(getOrDefault(OPENAPI_SPEC_LOCATION_ENV, DEFAULT_OPENAPI_SPEC_LOCATION));
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
