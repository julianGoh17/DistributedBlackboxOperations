package io.julian.gossip;

import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.models.UpdateResponse;
import io.julian.gossip.write.WriteHandler;
import io.julian.gossip.write.WriteReplyHandler;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageHandler {
    private final static Logger log = LogManager.getLogger(MessageHandler.class);
    private final WriteHandler writeHandler;
    private final WriteReplyHandler writeReplyHandler;

    public MessageHandler(final ServerClient client, final State state, final RegistryManager registry, final GossipConfiguration configuration, final ServerConfiguration serverConfiguration) {
        this.writeHandler = new WriteHandler(client, state, registry, configuration, serverConfiguration);
        this.writeReplyHandler = new WriteReplyHandler(client, state, registry, configuration);
    }

    public Future<Void> handleCoordinationMessage(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        switch (message.getMetadata().getType()) {
            case WriteHandler.UPDATE_REQUEST_TYPE:
                ServerConfiguration toServer = message.getDefinition().mapTo(ServerConfiguration.class);
                String messageID = message.getMetadata().getMessageID();
                return log.traceExit(writeReplyHandler.handleReply(messageID, message.getMessage(), toServer)
                    .compose(v -> writeHandler.forwardPost(messageID)));
            case WriteReplyHandler.WRITE_REPLY_TYPE:
                UpdateResponse response = message.getDefinition().mapTo(UpdateResponse.class);
                return log.traceExit(writeHandler.sendMessageIfNotInactive(response));
        }
        return log.traceExit(Future.succeededFuture());
    }

    public Future<Void> handleClientMessage(final ClientMessage message) {
        log.traceEntry(() -> message);
        return log.traceExit(this.writeHandler.dealWithClientMessage(message));
    }
}
