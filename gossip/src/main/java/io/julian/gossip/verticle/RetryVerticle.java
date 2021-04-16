package io.julian.gossip.verticle;

import io.julian.gossip.MessageHandler;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryVerticle extends AbstractVerticle {
    public static final String VERTICLE_ADDRESS = "retry";
    public static final String STOP_POSTFIX = "stop";
    public static final double BASE_TIMEOUT = 2;
    public static final double SECONDS_MODIFIER = 1000;
    public static final long BROADCAST_INTERVAL = 120 * (long) SECONDS_MODIFIER;

    private static final Logger log = LogManager.getLogger(RetryVerticle.class);
    private final MessageHandler handler;
    private final Vertx vertx;
    private final int verticleNumber;
    private final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationLetters;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private final AtomicInteger failedConsecutiveRequests = new AtomicInteger(0);

    public RetryVerticle(final MessageHandler handler, final Vertx vertx,
                         final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationLetters) {
        this.handler = handler;
        this.vertx = vertx;
        this.verticleNumber = vertx.deploymentIDs().size();
        this.deadCoordinationLetters = deadCoordinationLetters;
    }

    public void retryCoordinationMessages() {
        log.traceEntry();
        final AtomicBoolean inFlight = new AtomicBoolean(false);
        log.info("Starting to retry coordination loop");
        vertx.setPeriodic(500, id -> {
            if (isRunning.get()) {
                if (!inFlight.get() && deadCoordinationLetters.size() > 0) {
                    inFlight.set(true);
                    vertx.setTimer(getTimeout(failedConsecutiveRequests.get()).longValue(), v -> {
                        final CoordinationMessage message = deadCoordinationLetters.poll();
                        log.info("Retrying coordination message");
                        handler.handleCoordinationMessage(message)
                            .onSuccess(res -> {
                                failedConsecutiveRequests.set(0);
                                log.info("Succeeded retrying dead coordination letter");
                                inFlight.set(false);
                            })
                            .onFailure(cause -> {
                                log.info("Failed to retry dead coordination letter");
                                log.error(cause.getMessage());
                                failedConsecutiveRequests.incrementAndGet();
                                deadCoordinationLetters.add(message);
                                inFlight.set(false);
                            });
                    });
                }
            } else {
                vertx.cancelTimer(id);
            }
        });
        log.traceExit();
    }

    public void broadcastState() {
        log.traceEntry();
        log.info("Starting broadcast loop");
        vertx.setPeriodic(BROADCAST_INTERVAL, id -> {
            if (isRunning.get()) {
                broadcastState();
            } else {
                vertx.cancelTimer(id);
            }
        });
        log.traceExit();
    }

    @Override
    public void start() {
        log.traceEntry();
        log.info(String.format("Retry Verticle '%s-%d' has started", VERTICLE_ADDRESS, verticleNumber));
        vertx.eventBus().consumer(formatAddress(STOP_POSTFIX), v -> stopRetrying());
        retryCoordinationMessages();
        broadcastState();
        log.traceExit();
    }

    private void stopRetrying() {
        log.traceEntry();
        log.info(String.format("Stopping '%s-%d' verticle", VERTICLE_ADDRESS, verticleNumber));
        isRunning.set(false);
        log.traceExit();
    }

    public String formatAddress(final String postfix) {
        log.traceEntry(() -> postfix);
        return log.traceExit(String.format("%s-%s-%d", VERTICLE_ADDRESS, postfix, verticleNumber));
    }

    /**
     * Exposed for testing
     */
    public Double getTimeout(final int failedRequests) {
        log.traceEntry();
        return log.traceExit(Math.pow(BASE_TIMEOUT, failedRequests) * SECONDS_MODIFIER);
    }

    public int getFailedConsecutiveRequests() {
        log.traceEntry();
        return log.traceExit(failedConsecutiveRequests.get());
    }
}
