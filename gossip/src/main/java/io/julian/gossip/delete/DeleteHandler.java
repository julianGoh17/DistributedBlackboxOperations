package io.julian.gossip.delete;

import io.julian.gossip.AbstractHandler;
import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeleteHandler extends AbstractHandler {
    private final static Logger log = LogManager.getLogger(DeleteHandler.class);
    private final ServerConfiguration serverConfiguration;

    public final static String TYPE = "deleteRequest";

    public DeleteHandler(final ServerClient client, final State state, final RegistryManager registry, final GossipConfiguration configuration, final ServerConfiguration serverConfiguration) {
        super(client, state, registry, configuration);
        this.serverConfiguration = serverConfiguration;
    }

    public Future<Void> dealWithClientMessage(final ClientMessage message) {
        log.traceEntry(() -> message);
        state.deleteMessageIfInDatabase(message.getMessageId());
        return log.traceExit(sendMessage(getCoordinationMessage(message.getMessageId())));
    }

    public Future<Void> forwardDelete(final String messageId) {
        log.traceEntry(() -> messageId);
        state.deleteMessageIfInDatabase(messageId);
        return log.traceExit(sendMessage(getCoordinationMessage(messageId)));
    }

    private Future<Void> sendMessage(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        Promise<Void> delete = Promise.promise();
        final ServerConfiguration toServer = getNextServer();
        log.info(String.format("Attempting to forward '%s' of '%s' to '%s'", TYPE, message.getMetadata(), toServer));

        client.sendCoordinateMessageToServer(toServer, message)
            .onSuccess(v -> {
                log.info(String.format("Successfully forwarded '%s' of '%s' to '%s'", TYPE, message.getMetadata(), toServer));
                dealWithSucceededMessage(message);
                delete.complete();
            })
            .onFailure(cause -> {
                log.info(String.format("Failed to forward '%s' of '%s' to '%s'", TYPE, message.getMetadata(), toServer));
                log.error(cause);
                dealWithFailedMessage(message);
                delete.fail(cause);
            });

        return log.traceExit(delete.future());
    }

    public CoordinationMessage getCoordinationMessage(final String messageId) {
        log.traceEntry();
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.DELETE, messageId, TYPE),
            null,
            serverConfiguration.toJson()));
    }
}
