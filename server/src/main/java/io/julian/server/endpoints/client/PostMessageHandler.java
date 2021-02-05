package io.julian.server.endpoints.client;

import io.julian.server.api.DistributedAlgorithmVerticle;
import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.models.response.MessageIDResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class PostMessageHandler extends AbstractServerHandler {
    private static final Logger log =  LogManager.getLogger(PostMessageHandler.class);

    public static final String MESSAGE_KEY = "message";

    public void handle(final RoutingContext context, final ServerComponents components) {
        log.traceEntry(() -> context, () -> components);
        log.info("Entering " + PostMessageHandler.class.getName());
        JsonObject postedMessage = context.getBodyAsJson();
        UUID uuid = UUID.randomUUID();
        while (components.messageStore.hasUUID(uuid.toString())) {
            uuid = UUID.randomUUID();
        }

        final JsonObject userMessage = Optional.ofNullable(postedMessage)
            .map(mes -> mes.getJsonObject(MESSAGE_KEY))
            .orElse(new JsonObject());

        components.messageStore.putMessage(uuid.toString(), userMessage);

        sendResponseBack(context, 200, new MessageIDResponse(uuid.toString()).toJson());

        components.controller.addToInitialPostMessageQueue(userMessage);
        components.vertx.eventBus().send(
            DistributedAlgorithmVerticle.formatAddress(DistributedAlgorithmVerticle.INITIAL_POST_MESSAGE_POSTFIX),
            "");

        log.traceExit();
    }
}
