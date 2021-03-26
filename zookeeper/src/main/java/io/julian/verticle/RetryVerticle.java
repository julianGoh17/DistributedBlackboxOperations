package io.julian.verticle;

import io.julian.MessageHandler;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RetryVerticle extends AbstractVerticle {
    public static final String VERTICLE_ADDRESS = "retry";
    public static final String STOP_POSTFIX = "stop";
    public static final double BASE_TIMEOUT = 2;
    public static final double SECONDS_MODIFIER = 1000;

    private static final Logger log = LogManager.getLogger(RetryVerticle.class);
    private final MessageHandler handler;
    private final Vertx vertx;
    private final int verticleNumber;
    private final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationLetters;
    private final ConcurrentLinkedQueue<ClientMessage> deadClientLetters;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private AtomicInteger failedConsecutiveRequests = new AtomicInteger(0);

    public RetryVerticle(final MessageHandler handler, final Vertx vertx,
                         final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationLetters,
                         final ConcurrentLinkedQueue<ClientMessage> deadClientLetters) {
        this.handler = handler;
        this.vertx = vertx;
        this.verticleNumber = vertx.deploymentIDs().size();
        this.deadCoordinationLetters = deadCoordinationLetters;
        this.deadClientLetters = deadClientLetters;
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
                        sendCoordinationMessage(message)
                            .onComplete(res -> {
                                if (res.failed()) {
                                    log.info("Failed to retry dead coordination letter");
                                    log.error(res.cause().getMessage());
                                    failedConsecutiveRequests.incrementAndGet();
                                    deadCoordinationLetters.add(message);
                                } else {
                                    failedConsecutiveRequests.set(0);
                                    log.info("Succeeded retrying dead coordination letter");
                                }
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

    public void retryClientMessages() {
        log.traceEntry();
        final AtomicBoolean inFlight = new AtomicBoolean(false);
        log.info("Starting to retry client loop");
        vertx.setPeriodic(500, id -> {
            if (isRunning.get()) {
                if (!inFlight.get() && deadClientLetters.size() > 0) {
                    inFlight.set(true);
                    vertx.setTimer(getTimeout(failedConsecutiveRequests.get()).longValue(), v -> {
                        final ClientMessage message = deadClientLetters.poll();
                        log.info("Retrying client message");
                        handler.actOnInitialMessage(message)
                            .onComplete(res -> {
                                if (res.failed()) {
                                    log.info("Failed to retry dead client letter");
                                    log.error(res.cause().getMessage());
                                    failedConsecutiveRequests.incrementAndGet();
                                    deadClientLetters.add(message);
                                } else {
                                    log.info("Succeeded retrying dead client letter");
                                    failedConsecutiveRequests.set(0);
                                }
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

    public Future<Void> sendCoordinationMessage(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        if (handler.getController().getLabel().equals(LeadershipElectionHandler.LEADER_LABEL) || handler.getController().getLabel().isEmpty()) {
            return log.traceExit(broadcastToOtherServers(message));
        }
        return log.traceExit(sendToLeader(message));
    }

    public Future<Void> broadcastToOtherServers(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        log.info("Leader retrying broadcast of coordination message");
        Promise<Void> res = Promise.promise();
        List<Future> broadcast = handler.getRegistryManager().getOtherServers()
            .stream().map(config -> handler.getClient().sendCoordinateMessageToServer(config, message))
            .collect(Collectors.toList());

        CompositeFuture.all(broadcast)
            .onSuccess(v -> res.complete())
            .onFailure(cause -> res.fail(cause.getMessage()));

        return log.traceExit(res.future());
    }

    public Future<Void> sendToLeader(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        log.info("Follower retrying sending coordination message to leader");
        return log.traceExit(handler.getClient().sendCoordinateMessageToServer(handler.getRegistry().getLeaderServerConfiguration(), message));
    }

    @Override
    public void start() {
        log.traceEntry();
        log.info(String.format("Retry Verticle '%s-%d' has started", VERTICLE_ADDRESS, verticleNumber));
        vertx.eventBus().consumer(formatAddress(STOP_POSTFIX), v -> stopRetrying());
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
