package io.julian.server.endpoints.client;

import io.julian.server.api.DistributedAlgorithmVerticle;
import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
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
        JsonObject postedMessage = context.getBodyAsJson();
        UUID uuid = UUID.randomUUID();
        while (components.messageStore.hasUUID(uuid.toString())) {
            uuid = UUID.randomUUID();
        }

        final JsonObject userMessage = Optional.ofNullable(postedMessage)
            .map(mes -> mes.getJsonObject(MESSAGE_KEY))
            .orElse(new JsonObject());
        log.info(String.format("%s adding message to server", PostMessageHandler.class.getSimpleName()));

        components.messageStore.putMessage(uuid.toString(), userMessage);

        sendResponseBack(context, 200, new MessageIDResponse(uuid.toString()).toJson());

        if (components.verticle != null) {
            components.controller.addToClientMessageQueue(new ClientMessage(HTTPRequest.POST, userMessage, uuid.toString()));
            components.vertx.eventBus().send(
                components.verticle.formatAddress(DistributedAlgorithmVerticle.CLIENT_MESSAGE_POSTFIX),
                "");
        } else {
            log.info("Skipping adding to client queue as distributed algorithm not loaded");
        }

        log.traceExit();
    }
}
