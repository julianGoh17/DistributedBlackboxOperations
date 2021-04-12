package io.julian.zookeeper.synchronize;

import io.julian.server.api.client.ServerClient;
import io.julian.server.api.exceptions.NoIDException;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.Stage;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FollowerSynchronizeHandler {
    public static final String MESSAGE_ID = "followerSynchronizeMessage";

    private static final Logger log = LogManager.getLogger(FollowerSynchronizeHandler.class);
    private final Vertx vertx;
    private final State state;
    private final CandidateInformationRegistry registry;
    private final ServerClient client;
    private final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationMessages;

    public FollowerSynchronizeHandler(final Vertx vertx, final State state, final CandidateInformationRegistry registry, final ServerClient client, final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationMessages) {
        this.vertx = vertx;
        this.state = state;
        this.registry = registry;
        this.client = client;
        this.deadCoordinationMessages = deadCoordinationMessages;
    }

    public Future<Void> replyToLeader(final State leader) {
        log.traceEntry(() -> leader);

        Promise<Void> reply = Promise.promise();
        synchronizeWithLeaderState(leader)
            .onComplete(res -> {
                if (res.succeeded()) {
                    log.info("Succeeded leader synchronize");
                } else {
                    log.info("Failed to synchronize with leader");
                    log.error(res.cause());
                }
                client.sendCoordinateMessageToServer(registry.getLeaderServerConfiguration(), getCoordinationMessage())
                    .onSuccess(v -> {
                        state.setServerStage(Stage.WRITE);
                        reply.complete();
                    })
                    .onFailure(cause -> {
                        log.info("Failed to send synchronize reply to leader");
                        deadCoordinationMessages.add(getCoordinationMessage());
                        log.error(cause.getMessage());
                        reply.fail(cause);
                    });
            });
        return log.traceExit(reply.future());
    }

    public Future<Void> synchronizeWithLeaderState(final State leader) {
        log.traceEntry(() -> leader);
        Promise<Void> synchronize = Promise.promise();
        log.info("Synchronizing with leader state");
        vertx.executeBlocking(future -> {
            List<Proposal> missingProposals = findMissingProposals(leader);
            for (Proposal proposal : missingProposals) {
                ClientMessage message = proposal.getNewState();
                if (message.getRequest().equals(HTTPRequest.POST)) {
                    state.getMessageStore().putMessage(message.getMessageId(), message.getMessage());
                } else {
                    try {
                        state.getMessageStore().deleteMessageFromServer(message.getMessageId());
                    } catch (NoIDException e) {
                        log.info("Tried to delete message not in server, but ignoring");
                    }
                }
            }
            state.setState(leader);
            log.info("Finished synchronizing with leader state");
            future.complete();
        },
            res -> synchronize.complete());
        return log.traceExit(synchronize.future());
    }

    public List<Proposal> findMissingProposals(final State leader) {
        log.traceEntry(() -> leader);
        List<Proposal> proposals = new ArrayList<>();
        final Zxid currentId = new Zxid(state.getLeaderEpoch(), state.getCounter());
        log.info(String.format("Finding missing proposals later than %s from leader state", currentId));
        for (int i = 0; i < leader.getLastAcceptedIndex(); i++) {
            Proposal proposal = leader.getHistory().get(i);
            if (proposal.getTransactionId().isLaterThan(currentId)) {
                proposals.add(proposal);
            }
        }
        proposals.sort((a, b) -> Zxid.comparator(a.getTransactionId(), b.getTransactionId()));
        return log.traceExit(proposals);
    }

    public CoordinationMessage getCoordinationMessage() {
        log.traceEntry();
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.UNKNOWN, MESSAGE_ID, SynchronizeHandler.SYNCHRONIZE_TYPE),
            null,
            null));
    }

    public State getState() {
        log.traceEntry();
        return log.traceExit(state);
    }

    public ConcurrentLinkedQueue<CoordinationMessage> getDeadCoordinationMessages() {
        log.traceEntry();
        return log.traceExit(deadCoordinationMessages);
    }
}
