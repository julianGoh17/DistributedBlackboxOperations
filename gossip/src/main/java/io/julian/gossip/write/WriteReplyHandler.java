package io.julian.gossip.write;

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
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WriteReplyHandler extends AbstractHandler {
    private final static Logger log = LogManager.getLogger(WriteReplyHandler.class);
    public final static String WRITE_REPLY_TYPE = "writeReply";

    public WriteReplyHandler(final ServerClient client, final State state, final RegistryManager registry, final GossipConfiguration configuration) {
        super(client, state, registry, configuration);
    }

    public Future<Void> handleReply(final String messageId, final JsonObject message, final ServerConfiguration toServer) {
        log.traceEntry(() -> messageId, () -> message, () -> toServer);

        boolean hasMessage = true;
        if (!state.getMessageStore().hasUUID(messageId)) {
            state.addMessageIfNotInDatabase(messageId, message);
            hasMessage = false;
        }
        log.info(String.format("Attempting to reply post to '%s' about '%s'", toServer.toString(), messageId));

        CoordinationMessage sentMessage = getCoordinationMessage(messageId, hasMessage);

        return log.traceExit(sendResponseToServer(
            toServer,
            sentMessage,
            String.format("Successfully replied post to '%s' about '%s'", toServer.toString(), messageId),
            String.format("Failed to reply post to '%s' about '%s'", toServer.toString(), messageId)));
    }

    public CoordinationMessage getCoordinationMessage(final String messageId, final boolean hasMessage) {
        log.traceEntry(() -> messageId, () -> hasMessage);
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.POST, String.format("%s-reply", messageId), WRITE_REPLY_TYPE),
            null,
            new UpdateResponse(messageId, hasMessage).toJson()));
    }
}
