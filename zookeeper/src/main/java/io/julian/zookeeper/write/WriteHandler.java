package io.julian.zookeeper.write;

import io.julian.server.components.Controller;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WriteHandler {
    private static final Logger log = LogManager.getLogger(WriteHandler.class);

    private final Controller controller;
    private final State state;

    public WriteHandler(final Controller controller, final int majority, final Vertx vertx) {
        this.controller = controller;
        this.state = new State(vertx);
    }

    public boolean isLeader() {
        log.traceEntry();
        return log.traceExit(controller.getLabel().equals(LeadershipElectionHandler.LEADER_LABEL));
    }
}
