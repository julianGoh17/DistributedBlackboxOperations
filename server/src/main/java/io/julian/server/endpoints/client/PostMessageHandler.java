package io.julian.server.endpoints.client;

import io.julian.server.api.DistributedAlgorithmVerticle;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.response.MessageIDResponse;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class PostMessageHandler {
    public static final String MESSAGE_KEY = "message";

    private static final Logger log =  LogManager.getLogger(PostMessageHandler.class);

    public void handle(final RoutingContext context, final MessageStore messages, final Controller controller, final Vertx vertx) {
        log.traceEntry(() -> context, () -> messages);
        log.info("Entering " + PostMessageHandler.class.getName());
        JsonObject postedMessage = context.getBodyAsJson();
        UUID uuid = UUID.randomUUID();
        while (messages.hasUUID(uuid.toString())) {
            uuid = UUID.randomUUID();
        }

        final JsonObject userMessage = Optional.ofNullable(postedMessage)
            .map(mes -> mes.getJsonObject(MESSAGE_KEY))
            .orElse(new JsonObject());

        messages.putMessage(uuid.toString(), userMessage);

        context.response()
            .setStatusCode(200)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(new MessageIDResponse(uuid.toString()).toJson().encodePrettily());

        controller.addToInitialPostMessageQueue(userMessage);
        vertx.eventBus().send(
            DistributedAlgorithmVerticle.formatAddress(DistributedAlgorithmVerticle.INITIAL_POST_MESSAGE_POSTFIX),
            "");

        log.traceExit();
    }
}
