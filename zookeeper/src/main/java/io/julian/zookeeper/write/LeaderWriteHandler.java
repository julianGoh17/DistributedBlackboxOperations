package io.julian.zookeeper.write;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.AbstractHandler;
import io.julian.zookeeper.models.MessagePhase;
import io.julian.zookeeper.models.ShortenedExchange;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class LeaderWriteHandler extends AbstractHandler {
    private final Logger log = LogManager.getLogger(LeaderWriteHandler.class);
    public static final String TYPE = "state_update";

    private final LeaderProposalTracker proposalTracker;
    private final RegistryManager manager;
    private final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationMessages;

    public LeaderWriteHandler(final int majority, final ServerClient client, final RegistryManager manager, final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationMessages) {
        super(client);
        this.proposalTracker = new LeaderProposalTracker(majority);
        this.manager = manager;
        this.deadCoordinationMessages = deadCoordinationMessages;
    }

    public Future<Void> broadcastInitialProposal(final ClientMessage message, final Zxid id) {
        log.traceEntry(() -> message, () -> id);
        return log.traceExit(broadcastProposal(MessagePhase.ACK, message, id));
    }

    public Future<Void> broadcastCommit(final Zxid id) {
        log.traceEntry(() -> id);
        return log.traceExit(broadcastProposal(MessagePhase.COMMIT, null, id));
    }

    public Future<Void> broadcastProposal(final MessagePhase phase, final ClientMessage message, final Zxid id) {
        log.traceEntry(() -> phase, () -> message, () -> id);
        Promise<Void> broadcast = Promise.promise();

        log.info(String.format("Broadcasting '%s' update %s to servers", phase.toValue(), id.toString()));
        final CoordinationMessage broadcastMessage = createCoordinationMessage(phase, message, id);
        List<Future> futures = manager.getOtherServers()
            .stream()
            .map(server -> client.sendCoordinateMessageToServer(server, broadcastMessage))
            .collect(Collectors.toList());

        CompositeFuture.all(futures)
            .onSuccess(v -> {
                log.info(String.format("Successfully broadcast state update %s to servers", id));
                if (MessagePhase.ACK.equals(phase)) {
                    proposalTracker.addAcknowledgedProposalTracker(id);
                } else {
                    proposalTracker.addCommittedProposalTracker(id);
                }
                for (int i = 0; i < manager.getOtherServers().size(); i += 1) sendToMetricsCollector(200, broadcastMessage);
                broadcast.complete();
            })
            .onFailure(cause -> {
                log.info(String.format("Could not broadcast state update %s to servers", id));
                deadCoordinationMessages.add(broadcastMessage);
                for (int i = 0; i < manager.getOtherServers().size(); i += 1) sendToMetricsCollector(400, broadcastMessage);
                log.error(cause);
                broadcast.fail(cause);
            });

        return log.traceExit(broadcast.future());
    }

    public CoordinationMessage createCoordinationMessage(final MessagePhase phase, final ClientMessage message, final Zxid id) {
        log.traceEntry(() -> phase, () -> message, () -> id);

        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.UNKNOWN, id.toString(), TYPE),
            message != null ? message.toJson() : null,
            new ShortenedExchange(phase, id).toJson()));
    }

    public boolean addAcknowledgementAndCheckForMajority(final Zxid id) {
        log.traceEntry(() -> id);
        proposalTracker.addAcknowledgedProposal(id);
        if (proposalTracker.hasMajorityOfServersAcknowledgedProposal(id)) {
            log.info(String.format("Majority of servers has acknowledged '%s', deleting entry and moving to next stage", id));
            proposalTracker.removeAcknowledgedProposalTracker(id);
            return log.traceExit(true);
        } else if (proposalTracker.existsAcknowledgedProposalTracker(id)) {
            log.info(String.format("Waiting for more acknowledgements for '%s'", id));
        } else {
            log.info(String.format("Enough acknowledgements have arrived for '%s'", id));
        }

        return log.traceExit(false);
    }

    public boolean addCommitAcknowledgementAndCheckForMajority(final Zxid id) {
        log.traceEntry(() -> id);
        proposalTracker.addCommittedProposal(id);
        if (proposalTracker.hasMajorityOfServersCommittedProposal(id)) {
            log.info(String.format("Majority of servers has committed '%s', deleting entry and moving to next stage", id));
            proposalTracker.removeCommittedProposalTracker(id);
            return log.traceExit(true);
        } else if (proposalTracker.existsCommittedProposalTracker(id)) {
            log.info(String.format("Waiting for more committed acknowledgements for '%s'", id));
        } else {
            log.info(String.format("Enough committed acknowledgements have arrived for '%s'", id));
        }

        return log.traceExit(false);
    }

    public LeaderProposalTracker getProposalTracker() {
        log.traceEntry();
        return log.traceExit(proposalTracker);
    }

    public ConcurrentLinkedQueue<CoordinationMessage> getDeadCoordinationMessages() {
        log.traceEntry();
        return log.traceExit(deadCoordinationMessages);
    }
}
