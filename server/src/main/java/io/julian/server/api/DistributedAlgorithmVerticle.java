package io.julian.server.api;

import io.vertx.core.AbstractVerticle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DistributedAlgorithmVerticle extends AbstractVerticle {
    private static final Logger log = LogManager.getLogger(DistributedAlgorithmVerticle.class.getName());
    public static final String ALGORITHM_VERTICLE_ADDRESS = "algorithm";
    public static final String COORDINATE_MESSAGE_POSTFIX = "coordinate_message";
    public static final String INITIAL_POST_MESSAGE_POSTFIX = "initial_post_message";


    private final DistributedAlgorithm algorithm;

    public DistributedAlgorithmVerticle(final DistributedAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void start() {
        log.traceEntry();
        log.info(String.format("Algorithm Verticle '%s' Has Started", ALGORITHM_VERTICLE_ADDRESS));
        vertx.eventBus().consumer(formatAddress(COORDINATE_MESSAGE_POSTFIX), v -> algorithm.actOnCoordinateMessage());
        vertx.eventBus().consumer(formatAddress(INITIAL_POST_MESSAGE_POSTFIX), v -> algorithm.actOnInitialMessage());
        log.traceExit();
    }

    public static String formatAddress(final String postfix) {
        log.traceEntry(() -> postfix);
        return log.traceExit(String.format("%s-%s", ALGORITHM_VERTICLE_ADDRESS, postfix));
    }
}
