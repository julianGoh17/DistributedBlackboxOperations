package io.julian.gossip.write;

import io.julian.gossip.AbstractHandler;
import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.models.UpdateResponse;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.api.exceptions.SameIDException;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class WriteHandler extends AbstractHandler {
    private final static Logger log = LogManager.getLogger(WriteHandler.class);
    private final State state;
    private final RegistryManager registry;
    private final GossipConfiguration configuration;

    public final static String UPDATE_REQUEST_TYPE = "updateRequest";
    // TODO: Add retry verticle
    public WriteHandler(final ServerClient client, final State state, final RegistryManager registry, final GossipConfiguration configuration) {
        super(client);
        this.state = state;
        this.registry = registry;
        this.configuration = configuration;
    }

    public Future<Void> sendMessage(final UpdateResponse response) {
        log.traceEntry(() -> response);
        if (!response.getDoesContainId() || (response.getDoesContainId() && !shouldGoInactive())) {
            if (state.getMessages().hasUUID(response.getMessageId())) {
                log.info(String.format("Propagating '%s' to another server", response.getMessageId()));
                return log.traceExit(sendMessage(response.getMessageId(), state.getMessages().getMessage(response.getMessageId())));
            }
            log.info(String.format("Server doesn't contain '%s', will skip propagation of message", response.getMessageId()));
            return log.traceExit(Future.succeededFuture());
        }
        log.info(String.format("Server has chosen to go inactive for '%s'", response.getMessageId()));
        return log.traceExit(Future.succeededFuture());
    }

    public Future<Void> sendMessage(final ClientMessage message) {
        log.traceEntry(() -> message);
        try {
            state.getMessages().addMessageToServer(message.getMessageId(), message.getMessage());
        } catch (final SameIDException e) {
            log.info(String.format("Skipping adding '%s' message to server", message.getMessageId()));
        }
        return log.traceExit(sendMessage(message.getMessageId(), message.getMessage()));
    }

    public Future<Void> sendMessage(final String messageId, final JsonObject message) {
        log.traceEntry(() -> messageId, () -> message);
        Promise<Void> post = Promise.promise();
        ServerConfiguration toServer = getNextServer();
        log.info(String.format("Attempting to send '%s' to '%s'", messageId, toServer));
        CoordinationMessage sentMessage = getCoordinationMessage(message, messageId);
        client.sendCoordinateMessageToServer(toServer, sentMessage)
            .onSuccess(v -> {
                log.info(String.format("Successfully sent '%s' to '%s'", messageId, toServer));
                sendToMetricsCollector(200, sentMessage);
                post.complete();
            })
            .onFailure(cause -> {
                log.info(String.format("Failed to send '%s' to '%s'", messageId, toServer));
                log.error(cause.getMessage());
                sendToMetricsCollector(400, sentMessage);
                post.fail(cause);
            });

        return log.traceExit(post.future());
    }

    public CoordinationMessage getCoordinationMessage(final JsonObject message, final String messageId) {
        log.traceEntry(() -> message, () -> messageId);
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.POST, messageId, UPDATE_REQUEST_TYPE),
            message,
            null));
    }

    public ServerConfiguration getNextServer() {
        log.traceEntry();
        Random random = new Random();
        return log.traceExit(registry.getOtherServers().get(random.nextInt(registry.getOtherServers().size())));
    }

    public boolean shouldGoInactive() {
        log.traceEntry();
        Random random = new Random();
        return log.traceExit(random.nextFloat() < configuration.getInactiveProbability());
    }
}
