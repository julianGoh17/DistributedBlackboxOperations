package io.julian.gossip.synchronize;

import io.julian.gossip.AbstractHandler;
import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.models.SynchronizeUpdate;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SynchronizeHandler extends AbstractHandler {
    private final static Logger log = LogManager.getLogger(SynchronizeHandler.class);
    private final AtomicInteger count = new AtomicInteger();

    public final static String SYNCHRONIZE_TYPE = "synchronize_type";

    public SynchronizeHandler(final ServerClient client, final State state, final RegistryManager registry, final GossipConfiguration configuration) {
        super(client, state, registry, configuration);
    }

    public Future<Void> broadcastSynchronizeUpdate() {
        log.traceEntry();
        Promise<Void> broadcast = Promise.promise();
        count.incrementAndGet();
        final CoordinationMessage message = getCoordinateMessage();
        List<Future> updates = registry.getOtherServers().stream()
            .map(server -> sendSynchronizeUpdateTo(message, server))
            .collect(Collectors.toList());

        log.info("Attempting to broadcast synchronize update to all servers");
        CompositeFuture.all(updates)
            .onSuccess(v -> {
                log.info("Successfully broadcast synchronize update to all servers");
                broadcast.complete();
            })
            .onFailure(cause -> {
                log.info("Failed to broadcast synchronize update to all servers");
                log.error(cause);
                broadcast.fail(cause);
            });

        return log.traceExit(broadcast.future());
    }

    public Future<Void> sendSynchronizeUpdateTo(final CoordinationMessage message, final ServerConfiguration toServer) {
        log.traceEntry(() -> message, () -> toServer);
        log.info(String.format("Attempting to send synchronize update '%s' to '%s'", getMessageId(), toServer));
        return log.traceExit(sendResponseToServer(toServer,
            message,
            String.format("Successfully sent synchronize update '%s' to '%s'", getMessageId(), toServer),
            String.format("Failed to send synchronize update '%s' to '%s'", getMessageId(), toServer)
        ));
    }

    public CoordinationMessage getCoordinateMessage() {
        log.traceEntry();
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.POST, getMessageId(), SYNCHRONIZE_TYPE),
                null,
                new SynchronizeUpdate(state.getMessages(), state.getDeletedIds()).toJson())
        );
    }

    public String getMessageId() {
        log.traceEntry();
        return log.traceExit(String.format("synchronize-%d", count.get()));
    }
}
