package io.julian.zookeeper.write;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Controller;
import io.julian.server.models.control.ClientMessage;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class WriteHandler {
    private static final Logger log = LogManager.getLogger(WriteHandler.class);

    private final Controller controller;
    private final State state;
    private final LeaderWriteHandler leaderWrite;

    private final AtomicInteger leaderEpoch = new AtomicInteger();
    private final AtomicInteger counter = new AtomicInteger();

    public WriteHandler(final Controller controller, final CandidateInformationRegistry registry, final ServerClient client, final RegistryManager manager, final Vertx vertx) {
        this.controller = controller;
        this.state = new State(vertx);
        this.leaderWrite = new LeaderWriteHandler(getMajority(registry), client, manager);
    }

    public Future<Void> initialProposalUpdate(final ClientMessage message) {
        log.traceEntry(() -> message);
        final Zxid id = new Zxid(leaderEpoch.get(), counter.getAndIncrement());
        log.info(String.format("Adding proposal %s to history and broadcasting proposal", id));
        state.addProposal(new Proposal(message, id));
        return log.traceExit(leaderWrite.broadcastInitialProposal(message, id));
    }

    public Future<Void> addAcknowledgementAndAttemptToBroadcastCommit(final Zxid id) {
        log.traceEntry(() -> id);

        if (leaderWrite.addAcknowledgementAndCheckForMajority(id)) {
            log.info(String.format("Majority of servers acknowledged '%s', broadcasting commit", id));
            return log.traceExit(leaderWrite.broadcastCommit(id));
        }
        log.info(String.format("Will not broadcast commit for %s as conditions have not been met", id));
        return Future.succeededFuture();
    }

    public boolean isLeader() {
        log.traceEntry();
        return log.traceExit(controller.getLabel().equals(LeadershipElectionHandler.LEADER_LABEL));
    }

    public int getLeaderEpoch() {
        log.traceEntry();
        return log.traceExit(leaderEpoch.get());
    }

    public int getCounter() {
        log.traceEntry();
        return log.traceExit(counter.get());
    }

    public State getState() {
        log.traceEntry();
        return log.traceExit(state);
    }

    public LeaderProposalTracker getProposalTracker() {
        log.traceEntry();
        return log.traceExit(leaderWrite.getProposalTracker());
    }

    public int getMajority(final CandidateInformationRegistry registry) {
        log.traceEntry();
        return log.traceExit(registry.getCandidateNumberAndInformationMap().size() / 2 + 1);
    }
}
