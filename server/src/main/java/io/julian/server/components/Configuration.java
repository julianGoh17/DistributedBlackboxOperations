package io.julian.server.components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class Configuration {
    public static final Logger log = LogManager.getLogger(Configuration.class.getName());

    public static final String OPENAPI_SPEC_LOCATION_ENV = "OPENAPI_SPEC_LOCATION";
    public static final String OPENAPI_SPEC_LOCATION = "src/main/resources/ServerEndpoints.yaml";

    public static String getOpenapiSpecLocation() {
        log.traceEntry();
        return getOrDefault(OPENAPI_SPEC_LOCATION_ENV, OPENAPI_SPEC_LOCATION);
    }

    public static String getOrDefault(final String key, final String defaultVal) {
        log.traceEntry(() -> key, () -> defaultVal);
        return log.traceExit(Optional.ofNullable(System.getenv(key))
            .orElse(defaultVal));
    }
}
