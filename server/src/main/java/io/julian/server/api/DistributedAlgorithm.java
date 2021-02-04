package io.julian.server.api;

import io.julian.server.components.Controller;
import io.julian.server.models.coordination.CoordinationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DistributedAlgorithm {
    private static final Logger log = LogManager.getLogger(DistributedAlgorithm.class.getName());
    private final Controller controller;

    public DistributedAlgorithm(final Controller controller) {
        this.controller = controller;
    }

    public abstract void actOnCoordinateMessage();

    public abstract void actOnInitialMessage();

    public CoordinationMessage getCoordinationMessage() {
        log.traceEntry();
        return log.traceExit(controller.getCoordinationMessage());
    }

    public Controller getController() {
        log.traceEntry();
        return log.traceExit(controller);
    }
}
