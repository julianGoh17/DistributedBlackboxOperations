package io.julian.zookeeper.write;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.MessagePhase;
import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.ShortenedExchange;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class WriteHandler {
    private static final Logger log = LogManager.getLogger(WriteHandler.class);

    private final Controller controller;
    private final State state;
    private final LeaderWriteHandler leaderWrite;
    private final FollowerWriteHandler followerWrite;

    private final AtomicInteger leaderEpoch = new AtomicInteger();
    private final AtomicInteger counter = new AtomicInteger();

    public WriteHandler(final Controller controller, final MessageStore messageStore, final CandidateInformationRegistry registry, final ServerClient client, final RegistryManager manager, final Vertx vertx) {
        this.controller = controller;
        this.state = new State(vertx, messageStore);
        this.leaderWrite = new LeaderWriteHandler(getMajority(registry), client, manager);
        this.followerWrite = new FollowerWriteHandler(registry, client);
    }

    /*
     * Follower Methods
     */
    public Future<Void> acknowledgeLeader(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        final ClientMessage clientMessage = Optional.ofNullable(message.getMessage())
            .map(ClientMessage::fromJson)
            .orElse(null);
        final ShortenedExchange exchange = message.getDefinition().mapTo(ShortenedExchange.class);

        if (MessagePhase.ACK.equals(exchange.getPhase())) {
            log.info(String.format("Received State update %s from leader", exchange.getTransactionID()));
            state.addProposal(new Proposal(clientMessage, exchange.getTransactionID()));
            return log.traceExit(followerWrite.acknowledgeProposalToLeader(exchange.getTransactionID()));
        } else {
            log.info(String.format("Received commit state %s from leader", exchange.getTransactionID()));
            return log.traceExit(state.processStateUpdate(exchange.getTransactionID()));
        }
    }

    /*
     * Leader Methods
     */

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

    /*
     * Getters and Setters
     */

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
