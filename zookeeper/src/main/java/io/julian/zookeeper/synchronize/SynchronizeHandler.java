package io.julian.zookeeper.synchronize;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Controller;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SynchronizeHandler {
    private static final Logger log = LogManager.getLogger(SynchronizeHandler.class);
    public static final String SYNCHRONIZE_TYPE = "synchronize";

    private final LeaderSynchronizeHandler leader;
    private final FollowerSynchronizeHandler follower;
    private final Controller controller;

    public SynchronizeHandler(final Vertx vertx, final State state, final RegistryManager registryManager, final ServerClient client,
                              final CandidateInformationRegistry candidateInformationRegistry, final Controller controller) {
        this.leader = new LeaderSynchronizeHandler(state, registryManager, client);
        this.follower = new FollowerSynchronizeHandler(vertx, state, candidateInformationRegistry, client);
        this.controller = controller;
    }

    public Future<Void> broadcastState() {
        log.traceEntry();
        log.info("Leader broadcasting state to followers to synchronize");
        return log.traceExit(leader.broadcastState());
    }

    public Future<Void> handleCoordinationMessage(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        if (controller.getLabel().equals(LeadershipElectionHandler.LEADER_LABEL)) {
            log.info("Leader has received acknowledgement from the messages");
            leader.incrementAcknowledgement();
            return log.traceExit(Future.succeededFuture());
        }
        log.info("Follower has received synchronize state update from leader");
        State state = State.fromJson(message.getDefinition());
        return log.traceExit(this.follower.replyToLeader(state));
    }

    public boolean hasReceivedEnoughAcknowledgements() {
        log.traceEntry();
        return log.traceExit(leader.hasReceivedAcknowledgementsFromFollowers());
    }

    public LeaderSynchronizeHandler getLeaderSynchronizeHandler() {
        log.traceEntry();
        return log.traceExit(leader);
    }
}
