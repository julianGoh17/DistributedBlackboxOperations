package io.julian.zookeeper.write;

import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.AbstractHandler;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.models.MessagePhase;
import io.julian.zookeeper.models.ShortenedExchange;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

public class FollowerWriteHandler extends AbstractHandler  {
    private final static Logger log = LogManager.getLogger(FollowerWriteHandler.class);
    private final CandidateInformationRegistry registry;
    private final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationMessages;

    public final static String ACK_TYPE = "state_acknowledgement";
    public final static String FORWARD_TYPE = "forward";

    public FollowerWriteHandler(final CandidateInformationRegistry registry, final ServerClient client, final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationMessages) {
        super(client);
        this.registry = registry;
        this.deadCoordinationMessages = deadCoordinationMessages;
    }

    public Future<Void> forwardRequestToLeader(final ClientMessage message) {
        log.traceEntry(() -> message);
        Promise<Void> forward = Promise.promise();
        log.info(String.format("Attempting to forward %s to leader", message.getRequest()));
        final CoordinationMessage reply = new CoordinationMessage(new CoordinationMetadata(message.getRequest(), message.getMessageId(), FORWARD_TYPE), message.toJson(), null);
        client
            .sendCoordinateMessageToServer(registry.getLeaderServerConfiguration(), reply)
            .onSuccess(res -> {
                log.info(String.format("Successfully forwarded %s to leader", message.getRequest()));
                sendToMetricsCollector(200, reply);
                forward.complete();
            })
            .onFailure(cause -> {
                log.info(String.format("Failed to forwarded %s to leader", message.getRequest()));
                deadCoordinationMessages.add(reply);
                sendToMetricsCollector(400, reply);
                log.error(cause.getMessage());
                forward.fail(cause);
            });

        return log.traceExit(forward.future());
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
        final CoordinationMessage message = createCoordinationMessage(phase, id);
        client.sendCoordinateMessageToServer(registry.getLeaderServerConfiguration(), message)
            .onSuccess(res -> {
                log.info(String.format("Successfully sent '%s' reply for Zxid %s to leader", phase.toValue(), id.toString()));
                sendToMetricsCollector(200, message);
                reply.complete();
            })
            .onFailure(cause -> {
                log.info(String.format("Could not send '%s' reply for Zxid %s to leader", phase.toValue(), id.toString()));
                sendToMetricsCollector(400, message);
                deadCoordinationMessages.add(message);
                log.error(cause);
                reply.fail(cause);
            });

        return log.traceExit(reply.future());
    }

    public CoordinationMessage createCoordinationMessage(final MessagePhase phase, final Zxid id) {
        log.traceEntry(() -> phase, () -> id);
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.UNKNOWN, String.format("%s-%s", phase, id.toString()), ACK_TYPE),
            null,
            new ShortenedExchange(phase, id).toJson()
        ));
    }

    public ConcurrentLinkedQueue<CoordinationMessage> getDeadCoordinationMessages() {
        log.traceEntry();
        return log.traceExit(deadCoordinationMessages);
    }
}
