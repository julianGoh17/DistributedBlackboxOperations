package io.julian.gossip;

import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.verticle.RetryVerticle;
import io.julian.server.api.DistributedAlgorithm;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Gossip extends DistributedAlgorithm {
    private final Logger log = LogManager.getLogger(Gossip.class);
    private final State state;
    private final MessageHandler handler;
    private final RetryVerticle retryVerticle;

    public Gossip(final Controller controller, final MessageStore messageStore, final Vertx vertx) {
        super(controller, messageStore, vertx);
        this.state = new State(messageStore, getDeadCoordinationQueue());
        this.handler = new MessageHandler(getClient(), this.state, getRegistryManager(), new GossipConfiguration(), getController().getServerConfiguration());
        this.retryVerticle = new RetryVerticle(handler, vertx, getDeadCoordinationQueue());
        deployRetryVerticle();
    }

    // To start simulation, will send a coordination message to do candidate broadcast.
    @Override
    public void actOnCoordinateMessage() {
        log.traceEntry();
        CoordinationMessage message = getCoordinationMessage();
        this.handler.handleCoordinationMessage(message);
        log.traceExit();
    }

    @Override
    public void actOnInitialMessage() {
        log.traceEntry();
        ClientMessage message = getClientMessage();
        this.handler.handleClientMessage(message);
        log.traceExit();
    }

    public Future<String> deployRetryVerticle() {
        log.traceEntry();
        Promise<String> deployment = Promise.promise();
        vertx.deployVerticle(retryVerticle, res -> {
            if (res.succeeded()) {
                log.info("Succeeded deploying retry verticle");
                deployment.complete(res.result());
            } else {
                log.info("Failed to deploy retry verticle");
                log.error(res.cause().getMessage());
                deployment.fail(res.cause());
            }
        });
        return log.traceExit(deployment.future());
    }
}
