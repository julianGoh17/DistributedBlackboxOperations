package io.julian.gossip;

import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.metrics.collector.models.TrackedMessage;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public abstract class AbstractHandler {
    private final static Logger log = LogManager.getLogger(AbstractHandler.class);
    protected final ServerClient client;
    protected final State state;
    protected final RegistryManager registry;
    protected final GossipConfiguration configuration;

    public AbstractHandler(final ServerClient client, final State state, final RegistryManager registry, final GossipConfiguration configuration) {
        this.client = client;
        this.state = state;
        this.registry = registry;
        this.configuration = configuration;
    }

    protected void dealWithSucceededMessage(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        sendToMetricsCollector(200, message);
        log.traceExit();
    }

    protected void dealWithFailedMessage(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        sendToMetricsCollector(400, message);
        state.addToDeadLetters(message);
        log.traceExit();
    }

    public void sendToMetricsCollector(final int statusCode, final CoordinationMessage message) {
        log.traceEntry(() -> statusCode, () -> message);
        log.info(String.format("Attempting to send '%s' with status code '%d' to metrics collector", message.getMetadata().getMessageID(), statusCode));
        client.trackMessage(new TrackedMessage(statusCode, message.getMetadata().getMessageID(), message.toJson().toBuffer().getBytes().length))
            .onSuccess(v ->
                log.info(String.format("Successfully sent '%s' with status code '%d' to metrics collector", message.getMetadata().getMessageID(), statusCode)))
            .onFailure(cause -> {
                log.info(String.format("Failed to send '%s' with status code '%d' to metrics collector", message.getMetadata().getMessageID(), statusCode));
                log.error(cause.getMessage());
            });
        log.traceExit();
    }

    protected ServerConfiguration getNextServer() {
        log.traceEntry();
        Random random = new Random();
        return log.traceExit(registry.getOtherServers().get(random.nextInt(registry.getOtherServers().size())));
    }

    public boolean shouldBecomeInactive() {
        log.traceEntry();
        Random random = new Random();
        return log.traceExit(random.nextFloat() < configuration.getInactiveProbability());
    }
}
