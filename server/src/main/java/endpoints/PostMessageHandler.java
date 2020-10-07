package endpoints;

import api.MessageStore;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import models.MessageIDResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class PostMessageHandler {
    private static final Logger logger = LogManager.getLogger(PostMessageHandler.class);
    public static final String URI = "/client";

    public void handle(final RoutingContext context, final MessageStore messages) {
        logger.traceEntry(() -> context, () -> messages);
        logger.info("Entering PostMessage handler");
        JsonObject message = context.getBodyAsJson();
        UUID uuid = UUID.randomUUID();
        while (messages.hasUUID(uuid.toString())) {
            uuid = UUID.randomUUID();
        }

        messages.putMessage(uuid.toString(), message);

        context.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(new MessageIDResponse(uuid.toString()).toJson().encodePrettily());

        logger.traceExit();
    }
}
