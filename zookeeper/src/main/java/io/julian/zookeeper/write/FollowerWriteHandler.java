package io.julian.zookeeper.write;

import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.models.MessagePhase;
import io.julian.zookeeper.models.ShortenedExchange;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FollowerWriteHandler {
    private final static Logger log = LogManager.getLogger(FollowerWriteHandler.class);
    private final CandidateInformationRegistry registry;
    private final ServerClient client;

    public final static String TYPE = "state_acknowledgement";

    public FollowerWriteHandler(final CandidateInformationRegistry registry, final ServerClient client) {
        this.registry = registry;
        this.client = client;
    }

    public Future<Void> acknowledgeProposalToLeader(final Zxid id) {
        log.traceEntry(() -> id);
        return log.traceExit(replyToLeader(MessagePhase.ACK, id));
    }

    public Future<Void> acknowledgeCommitToLeader(final Zxid id) {
        log.traceEntry(() -> id);
        return log.traceExit(replyToLeader(MessagePhase.COMMIT, id));
    }

    private Future<Void> replyToLeader(final MessagePhase phase, final Zxid id) {
        log.traceEntry(() -> phase, () -> id);
        Promise<Void> reply = Promise.promise();
        log.info(String.format("Attempting to send '%s' reply for Zxid %s to leader", phase.toValue(), id.toString()));
        client.sendCoordinateMessageToServer(registry.getLeaderServerConfiguration(), createCoordinationMessage(phase, id))
            .onSuccess(res -> {
                log.info(String.format("Successfully sent '%s' reply for Zxid %s to leader", phase.toValue(), id.toString()));
                reply.complete();
            })
            .onFailure(cause -> {
                log.info(String.format("Could not send '%s' reply for Zxid %s to leader", phase.toValue(), id.toString()));
                log.error(cause);
                reply.fail(cause);
            });

        return log.traceExit(reply.future());
    }

    public CoordinationMessage createCoordinationMessage(final MessagePhase phase, final Zxid id) {
        log.traceEntry(() -> phase, () -> id);
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.UNKNOWN, TYPE),
            null,
            new ShortenedExchange(phase, id).toJson()
        ));
    }
}
