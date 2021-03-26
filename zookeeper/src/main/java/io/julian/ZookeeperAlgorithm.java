package io.julian;

import io.julian.server.api.DistributedAlgorithm;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.controller.State;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZookeeperAlgorithm extends DistributedAlgorithm {
    private final Logger log = LogManager.getLogger(ZookeeperAlgorithm.class);
    private final MessageHandler handler;

    public ZookeeperAlgorithm(final Controller controller, final MessageStore messageStore, final Vertx vertx) {
        super(controller, messageStore, vertx);
        this.handler = new MessageHandler(controller, messageStore, vertx, getRegistryManager(), getClient(), getDeadCoordinationQueue());
    }

    // To start simulation, will send a coordination message to do candidate broadcast.
    @Override
    public void actOnCoordinateMessage() {
        log.traceEntry();
        CoordinationMessage message = getCoordinationMessage();
        handler.actOnCoordinateMessage(message)
            .onFailure(cause -> {
                log.error(cause.getMessage());
                addToDeadCoordinationLetter(message);
            });
        log.traceExit();
    }

    @Override
    public void actOnInitialMessage() {
        log.traceEntry();
        ClientMessage message = getClientMessage();
        handler.actOnInitialMessage(message)
            .onFailure(v -> addToDeadClientLetter(message));
        log.traceExit();
    }

    public State getState() {
        log.traceEntry();
        return log.traceExit(handler.getState());
    }

    public MessageHandler getHandler() {
        log.traceEntry();
        return log.traceExit(handler);
    }
}
