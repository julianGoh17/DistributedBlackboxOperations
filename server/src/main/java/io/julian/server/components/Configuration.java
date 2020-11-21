package io.julian.server.components;

import io.julian.server.models.DistributedAlgorithmSettings;
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

    public static final String JAR_FILE_PATH_ENV = "JAR_FILE_PATH";
    public static final String PACKAGE_NAME_ENV = "PACKAGE_NAME";

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

    public static String getJarFilePath() {
        log.traceEntry();
        return log.traceExit(getOrDefault(JAR_FILE_PATH_ENV, ""));
    }

    public static boolean isJarFilePathEnvInstantiated() {
        log.traceEntry();
        return log.traceExit(isVariableInstantiated(JAR_FILE_PATH_ENV));
    }

    public static String getPackageName() {
        log.traceEntry();
        return log.traceExit(getOrDefault(PACKAGE_NAME_ENV, ""));
    }

    public static boolean isPackageNameEnvInstantiated() {
        log.traceEntry();
        return log.traceExit(isVariableInstantiated(PACKAGE_NAME_ENV));
    }

    public static DistributedAlgorithmSettings getDistributedAlgorithmSettings() {
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

    public static boolean isVariableInstantiated(final String key) {
        log.traceEntry(() -> key);
        return log.traceExit(Optional.ofNullable(System.getenv(key)))
            .isPresent();
    }
}
