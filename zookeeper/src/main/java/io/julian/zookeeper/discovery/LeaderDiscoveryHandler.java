package io.julian.zookeeper.discovery;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.Zxid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class LeaderDiscoveryHandler {
    private final static Logger log = LogManager.getLogger(LeadershipElectionHandler.class);

    private final AtomicInteger epochTracker = new AtomicInteger();
    private final AtomicInteger counterTracker = new AtomicInteger();

    private final AtomicInteger followerResponses = new AtomicInteger();
    private final State state;
    private final ServerClient client;
    private final RegistryManager manager;

    public LeaderDiscoveryHandler(final State state, final ServerClient client, final RegistryManager manager) {
        this.state = state;
        this.client = client;
        this.manager = manager;
    }

    public void processFollowerEpoch(final Zxid id) {
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
}
