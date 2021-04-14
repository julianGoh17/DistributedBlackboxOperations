package io.julian.gossip;

import io.julian.metrics.collector.models.TrackedMessage;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.coordination.CoordinationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractHandler {
    private final static Logger log = LogManager.getLogger(AbstractHandler.class);
    protected final ServerClient client;

    public AbstractHandler(final ServerClient client) {
        this.client = client;
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
}
