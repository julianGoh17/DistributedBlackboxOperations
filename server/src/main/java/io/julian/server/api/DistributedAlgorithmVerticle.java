package io.julian.server.api;

import io.vertx.core.AbstractVerticle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DistributedAlgorithmVerticle extends AbstractVerticle {
    private static final Logger log = LogManager.getLogger(DistributedAlgorithmVerticle.class.getName());
    public static final String ALGORITHM_VERTICLE_ADDRESS = "algorithm";
    public static final String CONSUME_MESSAGE_POSTFIX = "consume";

    private final DistributedAlgorithm algorithm;

    public DistributedAlgorithmVerticle(final DistributedAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void start() {
        log.traceEntry();
        log.info(String.format("Algorithm Verticle '%s' Has Started", ALGORITHM_VERTICLE_ADDRESS));
        vertx.eventBus().consumer(formatAddress(CONSUME_MESSAGE_POSTFIX), v -> algorithm.consumeMessage());
        log.traceExit();
    }

    public static String formatAddress(final String postfix) {
        log.traceEntry(() -> postfix);
        return log.traceExit(String.format("%s-%s", ALGORITHM_VERTICLE_ADDRESS, postfix));
    }
}