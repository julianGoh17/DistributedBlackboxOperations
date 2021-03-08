package io.julian.zookeeper.discovery;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LeaderDiscoveryHandler {
    public final static String LEADER_STATE_UPDATE_TYPE = "leader_state_update";
    private final static Logger log = LogManager.getLogger(LeadershipElectionHandler.class);

    private final AtomicInteger epochTracker = new AtomicInteger();
    private final AtomicInteger counterTracker = new AtomicInteger();

    private final AtomicInteger followerResponses = new AtomicInteger();
    private final State state;
    private final RegistryManager manager;
    private final ServerClient client;

    public LeaderDiscoveryHandler(final State state, final RegistryManager manager, final ServerClient client) {
        this.state = state;
        this.manager = manager;
        this.client = client;
    }

    public void processFollowerZXID(final Zxid id) {
        log.traceEntry(() -> id);
        log.info(String.format("Processing follower epoch response '%s'", id));
        followerResponses.incrementAndGet();
        if (epochTracker.get() < id.getEpoch()) {
            epochTracker.set(id.getEpoch());
            counterTracker.set(id.getCounter());
        } else if (epochTracker.get() == id.getEpoch() && counterTracker.get() < id.getCounter()) {
            counterTracker.set(id.getCounter());
        }
        log.traceExit();
    }

    public Future<Void> broadcastGatherZXID() {
        log.traceEntry();
        log.info("Broadcasting Discover ZXID to followers");
        return log.traceExit(broadcastCoordinateMessageToFollowers(createBroadcastMessage(DiscoveryHandler.DISCOVERY_TYPE)));
    }

    public Future<Void> broadcastLeaderState() {
        log.traceEntry();
        log.info("Broadcasting leader ZXID to followers");
        return log.traceExit(broadcastCoordinateMessageToFollowers(createStateUpdate()));
    }

    private Future<Void> broadcastCoordinateMessageToFollowers(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        Promise<Void> broadcast = Promise.promise();
        List<Future> res = manager.getOtherServers()
            .stream()
            .map(configuration -> client.sendCoordinateMessageToServer(configuration, message))
            .collect(Collectors.toList());

        CompositeFuture.all(res)
            .onSuccess(v -> broadcast.complete())
            .onFailure(cause -> {
                log.info("Failed to broadcast to followers");
                log.error(cause.getMessage());
                broadcast.fail(cause);
            });
        return log.traceExit(broadcast.future());
    }

    public void updateStateEpochAndCounter() {
        log.traceEntry();
        log.info(String.format("Updating leader epoch to '%d' and counter to '%d'", epochTracker.get(), counterTracker.get()));
        state.setLeaderEpoch(epochTracker.get());
        state.setCounter(counterTracker.get());
        log.traceExit();
    }

    public boolean hasEnoughResponses() {
        log.traceEntry();
        return log.traceExit(manager.getOtherServers().size() == followerResponses.get());
    }

    public void reset() {
        log.traceEntry();
        log.info("Resetting epoch and follower tracker");
        followerResponses.set(0);
        epochTracker.set(state.getLeaderEpoch());
        counterTracker.set(state.getCounter());
        log.traceExit();
    }

    public int getEpoch() {
        log.traceEntry();
        return log.traceExit(epochTracker.get());
    }

    public int getFollowerResponses() {
        log.traceEntry();
        return log.traceExit(followerResponses.get());
    }

    public int getCounter() {
        log.traceEntry();
        return log.traceExit(counterTracker.get());
    }

    public State getState() {
        log.traceEntry();
        return log.traceExit(this.state);
    }

    public CoordinationMessage createBroadcastMessage(final String type) {
        log.traceEntry(() -> type);
        return log.traceExit(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN, "", type), null, null));
    }

    public CoordinationMessage createStateUpdate() {
        log.traceEntry();
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.UNKNOWN, "", LEADER_STATE_UPDATE_TYPE),
            null,
            new Zxid(state.getLeaderEpoch(), state.getCounter()).toJson()));
    }
}