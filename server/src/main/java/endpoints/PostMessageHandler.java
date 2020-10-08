package endpoints;

import components.MessageStore;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import models.MessageIDResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class PostMessageHandler {
    public static final String messageKey = "message";
    public static final String URI = "/client";

    private static final Logger logger = LogManager.getLogger(PostMessageHandler.class);

    public void handle(final RoutingContext context, final MessageStore messages) {
        logger.traceEntry(() -> context, () -> messages);
        logger.info("Entering PostMessageHandler");
        JsonObject message = context.getBodyAsJson();
        UUID uuid = UUID.randomUUID();
        while (messages.hasUUID(uuid.toString())) {
            uuid = UUID.randomUUID();
        }

        messages.putMessage(uuid.toString(), Optional.ofNullable(message)
            .map(mes -> mes.getJsonObject(messageKey))
            .orElse(new JsonObject()));

        context.response()
            .setStatusCode(200)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(new MessageIDResponse(uuid.toString()).toJson().encodePrettily());

        logger.traceExit();
    }
}
