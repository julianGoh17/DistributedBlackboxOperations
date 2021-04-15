package io.julian.gossip.delete;

import io.julian.gossip.AbstractHandler;
import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.models.UpdateResponse;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.vertx.core.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeleteReplyHandler extends AbstractHandler {
    private final static Logger log = LogManager.getLogger(DeleteReplyHandler.class);

    public final static String TYPE = "deleteReply";

    public DeleteReplyHandler(final ServerClient client, final State state, final RegistryManager registry, final GossipConfiguration configuration) {
        super(client, state, registry, configuration);
    }

    public Future<Void> handleReply(final String messageId, final ServerConfiguration toServer) {
        log.traceEntry(() -> messageId, () -> toServer);

        boolean hasProcessed = true;
        if (state.getMessages().hasUUID(messageId)) {
            state.deleteMessageIfInDatabase(messageId);
            hasProcessed = false;
        }
        log.info(String.format("Attempting to reply delete to '%s' about '%s'", toServer.toString(), messageId));

        CoordinationMessage sentMessage = getCoordinationMessage(messageId, hasProcessed);
        return log.traceExit(sendResponseToServer(
            toServer,
            sentMessage,
            String.format("Successfully replied to delete '%s' about '%s'", toServer.toString(), messageId),
            String.format("Failed to reply delete to '%s' about '%s'", toServer.toString(), messageId)));
    }

    public CoordinationMessage getCoordinationMessage(final String messageId, final boolean hasProcessedId) {
        log.traceEntry(() -> messageId, () -> hasProcessedId);
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.DELETE, String.format("%s-delete-reply", messageId), TYPE),
            null,
            new UpdateResponse(messageId, hasProcessedId).toJson()
        ));
    }
}
