package io.julian.gossip;

import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.delete.DeleteHandler;
import io.julian.gossip.delete.DeleteReplyHandler;
import io.julian.gossip.models.SynchronizeUpdate;
import io.julian.gossip.models.UpdateResponse;
import io.julian.gossip.synchronize.SynchronizeHandler;
import io.julian.gossip.write.WriteHandler;
import io.julian.gossip.write.WriteReplyHandler;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageHandler {
    private final static Logger log = LogManager.getLogger(MessageHandler.class);
    private final WriteHandler writeHandler;
    private final WriteReplyHandler writeReplyHandler;
    private final DeleteHandler deleteHandler;
    private final DeleteReplyHandler deleteReplyHandler;
    private final SynchronizeHandler synchronizeHandler;

    public MessageHandler(final ServerClient client, final State state, final RegistryManager registry, final GossipConfiguration configuration, final ServerConfiguration serverConfiguration, final Vertx vertx) {
        this.writeHandler = new WriteHandler(client, state, registry, configuration, serverConfiguration);
        this.writeReplyHandler = new WriteReplyHandler(client, state, registry, configuration);
        this.deleteHandler = new DeleteHandler(client, state, registry, configuration, serverConfiguration);
        this.deleteReplyHandler = new DeleteReplyHandler(client, state, registry, configuration);
        this.synchronizeHandler = new SynchronizeHandler(client, state, registry, configuration, vertx);
    }

    public Future<Void> handleCoordinationMessage(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        switch (message.getMetadata().getType()) {
            case WriteHandler.UPDATE_REQUEST_TYPE:
                ServerConfiguration toServer = message.getDefinition().mapTo(ServerConfiguration.class);
                String postMessageId = message.getMetadata().getMessageID();
                return log.traceExit(writeReplyHandler.handleReply(postMessageId, message.getMessage(), toServer)
                    .compose(v -> writeHandler.forwardPost(postMessageId)));
            case WriteReplyHandler.WRITE_REPLY_TYPE:
                UpdateResponse postResponse = message.getDefinition().mapTo(UpdateResponse.class);
                return log.traceExit(writeHandler.sendPostIfNotInactive(postResponse));
            case DeleteHandler.DELETE_UPDATE_TYPE:
                final ServerConfiguration receivedServer = message.getDefinition().mapTo(ServerConfiguration.class);
                String deleteMessageId = message.getMetadata().getMessageID();
                return log.traceExit(deleteReplyHandler.handleReply(deleteMessageId, receivedServer)
                    .compose(v -> deleteHandler.forwardDelete(deleteMessageId)));
            case DeleteReplyHandler.DELETE_REPLY_TYPE:
                UpdateResponse deleteResponse = message.getDefinition().mapTo(UpdateResponse.class);
                return log.traceExit(deleteHandler.sendDeleteIfNotInactive(deleteResponse));
            case SynchronizeHandler
                    .SYNCHRONIZE_TYPE:
                SynchronizeUpdate synchronizeUpdate = SynchronizeUpdate.fromJson(message.getDefinition());
                return log.traceExit(synchronizeHandler.synchronizeState(synchronizeUpdate));
        }
        return log.traceExit(Future.succeededFuture());
    }

    public Future<Void> handleClientMessage(final ClientMessage message) {
        log.traceEntry(() -> message);
        if (HTTPRequest.DELETE.equals(message.getRequest())) {
            return log.traceExit(this.deleteHandler.dealWithClientMessage(message));
        }
        return log.traceExit(this.writeHandler.dealWithClientMessage(message));
    }

    public Future<Void> broadcastState() {
        log.traceEntry();
        return log.traceExit(synchronizeHandler.broadcastSynchronizeUpdate());
    }
}
