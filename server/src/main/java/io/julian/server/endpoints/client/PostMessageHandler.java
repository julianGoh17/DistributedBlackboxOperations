package io.julian.server.endpoints.client;

import io.julian.server.api.DistributedAlgorithmVerticle;
import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.models.response.MessageIDResponse;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class PostMessageHandler extends AbstractServerHandler {
    private static final Logger log =  LogManager.getLogger(PostMessageHandler.class);

    public static final String MESSAGE_KEY = "message";

    public Future<String> handle(final RoutingContext context, final ServerComponents components) {
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

        context.response()
            .setStatusCode(200)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(new MessageIDResponse(uuid.toString()).toJson().encodePrettily());

        components.controller.addToInitialPostMessageQueue(userMessage);
        components.vertx.eventBus().send(
            DistributedAlgorithmVerticle.formatAddress(DistributedAlgorithmVerticle.INITIAL_POST_MESSAGE_POSTFIX),
            "");

        return log.traceExit(Future.succeededFuture());
    }
}
