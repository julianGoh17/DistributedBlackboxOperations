package io.julian.gossip.write;

import io.julian.gossip.AbstractHandler;
import io.julian.gossip.models.UpdateResponse;
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

public class WriteReplyHandler extends AbstractHandler {
    private final static Logger log = LogManager.getLogger(WriteReplyHandler.class);

    public final static String WRITE_REPLY_TYPE = "writeReply";

    public WriteReplyHandler(final ServerClient client) {
        super(client);
    }

    public Future<Void> handleReply(final ClientMessage message, final ServerConfiguration toServer) {
        log.traceEntry(() -> message, () -> toServer);
        Promise<Void> reply = Promise.promise();

        boolean hasMessage = true;
        log.info(String.format("Attempting to reply to '%s' about '%s'", toServer.toString(), message.getMessageId()));

        CoordinationMessage sentMessage = getCoordinationMessage(message.getMessageId(), hasMessage);
        client.sendCoordinateMessageToServer(toServer, sentMessage)
            .onSuccess(v -> {
                log.info(String.format("Successfully replied to '%s' about '%s'", toServer.toString(), message.getMessageId()));
                sendToMetricsCollector(200, sentMessage);
                reply.complete();
            })
            .onFailure(cause -> {
                log.info(String.format("Failed to reply to '%s' about '%s'", toServer.toString(), message.getMessageId()));
                log.error(cause);
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
