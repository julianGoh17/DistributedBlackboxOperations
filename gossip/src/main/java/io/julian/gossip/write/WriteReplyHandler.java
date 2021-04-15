package io.julian.gossip.write;

import io.julian.gossip.AbstractHandler;
import io.julian.gossip.components.State;
import io.julian.gossip.models.UpdateResponse;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WriteReplyHandler extends AbstractHandler {
    private final static Logger log = LogManager.getLogger(WriteReplyHandler.class);
    public final static String WRITE_REPLY_TYPE = "writeReply";

    public WriteReplyHandler(final ServerClient client, final State state) {
        super(client, state);
    }

    public Future<Void> handleReply(final String messageId, final JsonObject message, final ServerConfiguration toServer) {
        log.traceEntry(() -> messageId, () -> message, () -> toServer);
        Promise<Void> reply = Promise.promise();

        boolean hasMessage = true;
        if (!state.getMessages().hasUUID(messageId)) {
            state.addMessageIfNotInDatabase(messageId, message);
            hasMessage = false;
        }
        log.info(String.format("Attempting to reply to '%s' about '%s'", toServer.toString(), messageId));

        CoordinationMessage sentMessage = getCoordinationMessage(messageId, hasMessage);
        client.sendCoordinateMessageToServer(toServer, sentMessage)
            .onSuccess(v -> {
                log.info(String.format("Successfully replied to '%s' about '%s'", toServer.toString(), messageId));
                sendToMetricsCollector(200, sentMessage);
                reply.complete();
            })
            .onFailure(cause -> {
                log.info(String.format("Failed to reply to '%s' about '%s'", toServer.toString(), messageId));
                log.error(cause);
                state.addToDeadLetters(sentMessage);
                sendToMetricsCollector(400, sentMessage);
                reply.fail(cause);
            });

        return log.traceExit(reply.future());
    }

    public CoordinationMessage getCoordinationMessage(final String messageId, final boolean hasMessage) {
        log.traceEntry(() -> messageId, () -> hasMessage);
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.POST, String.format("%s-reply", messageId), WRITE_REPLY_TYPE),
            null,
            new UpdateResponse(messageId, hasMessage).toJson()));
    }
}
