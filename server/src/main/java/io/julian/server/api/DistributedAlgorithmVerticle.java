package io.julian.server.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DistributedAlgorithmVerticle extends AbstractVerticle {
    private static final Logger log = LogManager.getLogger(DistributedAlgorithmVerticle.class.getName());
    public static final String ALGORITHM_VERTICLE_ADDRESS = "algorithm";
    public static final String COORDINATE_MESSAGE_POSTFIX = "coordinate_message";
    public static final String CLIENT_MESSAGE_POSTFIX = "client_message";

    private final DistributedAlgorithm algorithm;
    private final int verticleNumber;

    public DistributedAlgorithmVerticle(final DistributedAlgorithm algorithm, final Vertx vertx) {
        this.algorithm = algorithm;
        this.verticleNumber = vertx.deploymentIDs().size();
    }

    @Override
    public void start() {
        log.traceEntry();
        log.info(String.format("Algorithm Verticle '%s' Has Started", ALGORITHM_VERTICLE_ADDRESS));
        vertx.eventBus().consumer(formatAddress(COORDINATE_MESSAGE_POSTFIX), v -> algorithm.actOnCoordinateMessage());
        vertx.eventBus().consumer(formatAddress(CLIENT_MESSAGE_POSTFIX), v -> algorithm.actOnInitialMessage());
        log.traceExit();
    }

    public String formatAddress(final String postfix) {
        log.traceEntry(() -> postfix);
        return log.traceExit(String.format("%s-%s-%d", ALGORITHM_VERTICLE_ADDRESS, postfix, verticleNumber));
    }
}
