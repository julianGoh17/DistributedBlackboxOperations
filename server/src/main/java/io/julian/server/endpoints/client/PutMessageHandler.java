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

public class PutMessageHandler extends AbstractServerHandler {
    private static final Logger log = LogManager.getLogger(PutMessageHandler.class);

    public void handle(final RoutingContext context, final ServerComponents components) {
        log.traceEntry(() -> context, () -> components);
        String originalId = Optional.ofNullable(context.queryParam("originalId"))
            .map(params -> params.get(0))
            .orElse(null);
        String newId = Optional.ofNullable(context.queryParam("newId"))
            .map(params -> params.get(0))
            .orElse(null);
        if (components.messageStore.hasUUID(originalId)) {
            log.info(String.format("Found entry for uuid '%s', updating message.", originalId));
            final JsonObject storedMessage = components.messageStore.getMessage(originalId);
            components.messageStore.removeMessage(originalId);
            components.messageStore.putMessage(newId, storedMessage);

            components.controller.addToClientMessageQueue(new ClientMessage(HTTPRequest.PUT, storedMessage, originalId, newId));
            components.vertx.eventBus().send(
                DistributedAlgorithmVerticle.formatAddress(DistributedAlgorithmVerticle.CLIENT_MESSAGE_POSTFIX),
                "");

            sendResponseBack(context, 200, new MessageIDResponse(originalId).toJson());
        } else {
            String errorMessage = String.format("Could not find entry for uuid '%s'", originalId);
            log.error(errorMessage);
            context.fail(404, new Exception(errorMessage));
        }

        log.traceExit();
    }
}
