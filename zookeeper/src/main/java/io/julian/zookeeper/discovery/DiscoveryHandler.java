package io.julian.zookeeper.discovery;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Controller;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DiscoveryHandler {
    public static final String NOT_ENOUGH_RESPONSES_ERROR = "Leader has not received enough follower ZXID responses";
    public static final String DISCOVERY_TYPE = "discovery";

    private final static Logger log = LogManager.getLogger(DiscoveryHandler.class);
    private final Controller controller;
    private final FollowerDiscoveryHandler followerHandler;
    private final LeaderDiscoveryHandler leaderHandler;
    private boolean hasBroadcastFollowerZXID = false;

    public DiscoveryHandler(final Controller controller, final State state, final CandidateInformationRegistry candidateInformationRegistry,
                            final RegistryManager registryManager, final ServerClient client) {
        this.controller = controller;
        this.followerHandler = new FollowerDiscoveryHandler(state, candidateInformationRegistry, client);
        this.leaderHandler = new LeaderDiscoveryHandler(state, registryManager, client);
    }

    public void reset() {
        log.traceEntry();
        log.info("Resetting DiscoveryHandler");
        hasBroadcastFollowerZXID = controller.getLabel().equals(LeadershipElectionHandler.FOLLOWER_LABEL);
        leaderHandler.reset();
        log.traceExit();
    }

    public Future<Void> broadcastToFollowers() {
        log.traceEntry();
        hasBroadcastFollowerZXID = true;
        return log.traceExit(leaderHandler.broadcastGatherZXID());
    }

    public Future<Void> handleCoordinationMessage(final CoordinationMessage coordinationMessage) {
        log.traceEntry(() -> coordinationMessage);
        if (controller.getLabel().equals(LeadershipElectionHandler.LEADER_LABEL)) {
            final Zxid id = coordinationMessage.getDefinition().mapTo(Zxid.class);
            log.info("Leader processing follower ZXID broadcast");
            leaderHandler.processFollowerZXID(id);
            if (leaderHandler.hasEnoughResponses()) {
                log.info("Leader has received ZXID broadcast from all followers");
                leaderHandler.updateStateEpochAndCounter();
                return log.traceExit(leaderHandler.broadcastLeaderState());
            }
            return log.traceExit(Future.failedFuture(NOT_ENOUGH_RESPONSES_ERROR));
        } else {
            if (coordinationMessage.getMetadata().getType().equals(LeaderDiscoveryHandler.LEADER_STATE_UPDATE_TYPE)) {
                log.info("Received leader update state request");
                followerHandler.updateToLeaderState(coordinationMessage.getDefinition().mapTo(Zxid.class));
                return log.traceExit(Future.succeededFuture());
            } else {
                log.info("Follower sending latest ZXID to leader");
                return log.traceExit(followerHandler.replyToLeader());
            }
        }
    }

    public boolean hasBroadcastFollowerZXID() {
        log.traceEntry();
        return log.traceExit(hasBroadcastFollowerZXID);
    }

    public State getState() {
        log.traceEntry();
        return log.traceExit(leaderHandler.getState());
    }

    public LeaderDiscoveryHandler getLeaderHandler() {
        log.traceEntry();
        return log.traceExit(leaderHandler);
    }


    public FollowerDiscoveryHandler getFollowerHandler() {
        log.traceEntry();
        return log.traceExit(followerHandler);
    }
}
