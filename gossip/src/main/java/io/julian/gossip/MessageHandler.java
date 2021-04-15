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

import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageHandler {
    private final static Logger log = LogManager.getLogger(MessageHandler.class);
    private final WriteHandler writeHandler;
    private final WriteReplyHandler writeReplyHandler;

    public MessageHandler(final ServerClient client, final State state, final RegistryManager registry, final GossipConfiguration configuration, final ServerConfiguration serverConfiguration, final ConcurrentLinkedQueue<CoordinationMessage> deadLetterQueue) {
        this.writeHandler = new WriteHandler(client, state, registry, configuration, serverConfiguration);
        this.writeReplyHandler = new WriteReplyHandler(client, state);
    }

    public Future<Void> handleCoordinationMessage(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        switch (message.getMetadata().getType()) {
            case WriteHandler.UPDATE_REQUEST_TYPE:
                ClientMessage clientMessage = ClientMessage.fromJson(message.getMessage());
                ServerConfiguration toServer = message.getDefinition().mapTo(ServerConfiguration.class);
                return log.traceExit(writeReplyHandler.handleReply(clientMessage, toServer)
                    .compose(v -> writeHandler.sendMessage(clientMessage)));
            case WriteReplyHandler.WRITE_REPLY_TYPE:
                UpdateResponse response = message.getDefinition().mapTo(UpdateResponse.class);
                return log.traceExit(writeHandler.sendMessage(response));
        }
        return log.traceExit(Future.succeededFuture());
    }
}
