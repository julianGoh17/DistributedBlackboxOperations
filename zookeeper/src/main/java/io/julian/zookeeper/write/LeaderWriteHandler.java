package io.julian.zookeeper.write;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.models.MessagePhase;
import io.julian.zookeeper.models.ShortenedExchange;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class LeaderWriteHandler {
    private final Logger log = LogManager.getLogger(LeaderWriteHandler.class);

    public static final String TYPE = "state_update";

    private final LeaderProposalTracker proposalTracker;
    private final ServerClient client;
    private final RegistryManager manager;

    public LeaderWriteHandler(final int majority, final ServerClient client, final RegistryManager manager) {
        this.proposalTracker = new LeaderProposalTracker(majority);
        this.client = client;
        this.manager = manager;
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
        List<Future> futures = manager.getOtherServers()
            .stream()
            .map(server -> client.sendCoordinateMessageToServer(server, createCoordinationMessage(phase, message, id)))
            .collect(Collectors.toList());

        CompositeFuture.all(futures)
            .onSuccess(v -> {
                log.info(String.format("Successfully broadcast state update %s to servers", id));
                broadcast.complete();
            })
            .onFailure(cause -> {
                log.info(String.format("Could not broadcast state update %s to servers", id));
                log.error(cause);
                broadcast.fail(cause);
            });

        return log.traceExit(broadcast.future());
    }

    public CoordinationMessage createCoordinationMessage(final MessagePhase phase, final ClientMessage message, final Zxid id) {
        log.traceEntry(() -> phase, () -> message, () -> id);
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.UNKNOWN, TYPE),
            message != null ? message.toJson() : null,
            new ShortenedExchange(phase, id).toJson()));
    }
}
