package io.julian.gossip;

import io.julian.server.api.DistributedAlgorithm;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Gossip extends DistributedAlgorithm {
    private final Logger log = LogManager.getLogger(Gossip.class);

    public Gossip(final Controller controller, final MessageStore messageStore, final Vertx vertx) {
        super(controller, messageStore, vertx);
    }

    // To start simulation, will send a coordination message to do candidate broadcast.
    @Override
    public void actOnCoordinateMessage() {
        log.traceEntry();
        CoordinationMessage message = getCoordinationMessage();
        log.traceExit();
    }

    @Override
    public void actOnInitialMessage() {
        log.traceEntry();
        ClientMessage message = getClientMessage();
        log.traceExit();
    }
}
