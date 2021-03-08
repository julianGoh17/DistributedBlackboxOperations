package io.julian.zookeeper.write;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
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

public class WriteHandler {
    private static final Logger log = LogManager.getLogger(WriteHandler.class);

    private final Controller controller;
    private final State state;
    private final LeaderWriteHandler leaderWrite;
    private final FollowerWriteHandler followerWrite;

    public WriteHandler(final Controller controller, final MessageStore messageStore, final CandidateInformationRegistry registry, final ServerClient client, final RegistryManager manager, final Vertx vertx) {
        this.controller = controller;
        this.state = new State(vertx, messageStore);
        this.leaderWrite = new LeaderWriteHandler(getMajority(registry), client, manager);
        this.followerWrite = new FollowerWriteHandler(registry, client);
    }

    public Future<Void> handleCoordinationMessage(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        if (controller.getLabel().equals(LeadershipElectionHandler.LEADER_LABEL)) {
            log.info("WriteHandler handling coordination message as leader");
            ClientMessage clientMessage = ClientMessage.fromJson(message.getMessage());
            if (HTTPRequest.POST.equals(message.getMetadata().getRequest()) || HTTPRequest.DELETE.equals(message.getMetadata().getRequest())) {
                return log.traceExit(initialProposalUpdate(clientMessage));
            }
            ShortenedExchange exchange = message.getDefinition() != null ? message.getDefinition().mapTo(ShortenedExchange.class) : null;
            return log.traceExit(addAcknowledgementAndAttemptToBroadcastCommit(exchange.getTransactionID()));
        }
        log.info("WriteHandler handling coordination message as follower");
        return log.traceExit(acknowledgeLeader(message));
    }

    public Future<Void> handleClientMessage(final ClientMessage message) {
        log.traceEntry(() -> message);
        if (controller.getLabel().equals(LeadershipElectionHandler.LEADER_LABEL)) {
            log.info("Leader broadcasting update to followers");
            return log.traceExit(initialProposalUpdate(message));
        }
        log.info("Follower forwarding to leader");
        return log.traceExit(followerWrite.forwardRequestToLeader(message));
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
        final Zxid id = new Zxid(state.getLeaderEpoch(), state.getAndIncrementCounter());
        log.info(String.format("Adding proposal %s to history and broadcasting proposal", id));
        return log.traceExit(state.addProposal(new Proposal(message, id))
            .compose(v -> state.processStateUpdate(id))
            .compose(v2 -> leaderWrite.broadcastInitialProposal(message, id)));
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

    public Controller getController() {
        log.traceEntry();
        return log.traceExit(controller);
    }
}
