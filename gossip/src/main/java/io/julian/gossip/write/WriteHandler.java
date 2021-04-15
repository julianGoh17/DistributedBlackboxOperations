package io.julian.gossip.write;

import io.julian.gossip.AbstractHandler;
import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.models.UpdateResponse;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WriteHandler extends AbstractHandler {
    private final static Logger log = LogManager.getLogger(WriteHandler.class);
    private final ServerConfiguration serverConfiguration;

    public final static String UPDATE_REQUEST_TYPE = "updateRequest";

    public WriteHandler(final ServerClient client, final State state, final RegistryManager registry, final GossipConfiguration configuration, final ServerConfiguration serverConfiguration) {
        super(client, state, registry, configuration);
        this.serverConfiguration = serverConfiguration;
    }

    public Future<Void> sendPostIfNotInactive(final UpdateResponse response) {
        log.traceEntry(() -> response);
        if (!response.getHasProcessedId() || !shouldBecomeInactive()) {
            return log.traceExit(forwardPost(response.getMessageId()));
        }
        state.addInactivePostId(response.getMessageId());
        log.info(String.format("Server has chosen to go inactive for post '%s'", response.getMessageId()));
        return log.traceExit(Future.succeededFuture());
    }

    public Future<Void> forwardPost(final String messageId) {
        log.traceEntry(() -> messageId);
        if (state.getMessages().hasUUID(messageId) && !state.isInactivePostId(messageId)) {
            log.info(String.format("Propagating post '%s' to another server", messageId));
            return log.traceExit(sendMessage(messageId, state.getMessages().getMessage(messageId)));
        }
        log.info(String.format("'%s' is an inactive key, will skip propagation of post message", messageId));
        return log.traceExit(Future.succeededFuture());
    }

    public Future<Void> dealWithClientMessage(final ClientMessage message) {
        log.traceEntry(() -> message);
        state.addMessageIfNotInDatabase(message.getMessageId(), message.getMessage());
        return log.traceExit(sendMessage(message.getMessageId(), message.getMessage()));
    }

    public Future<Void> sendMessage(final String messageId, final JsonObject message) {
        log.traceEntry(() -> messageId, () -> message);
        ServerConfiguration toServer = getNextServer();
        log.info(String.format("Attempting to send '%s' to '%s'", messageId, toServer));
        CoordinationMessage sentMessage = getCoordinationMessage(message, messageId);

        return log.traceExit(sendResponseToServer(
            toServer,
            sentMessage,
            String.format("Successfully sent '%s' to '%s'", messageId, toServer),
            String.format("Failed to send '%s' to '%s'", messageId, toServer)));
    }

    public CoordinationMessage getCoordinationMessage(final JsonObject message, final String messageId) {
        log.traceEntry(() -> message, () -> messageId);
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.POST, messageId, UPDATE_REQUEST_TYPE),
            message,
            serverConfiguration.toJson()));
    }
}
