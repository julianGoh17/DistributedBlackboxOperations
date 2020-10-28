package io.julian.server.endpoints;

import io.julian.server.components.MessageStore;
import io.julian.server.models.MessageResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetMessageHandler {
    private static final Logger log = LogManager.getLogger(GetMessageHandler.class);

    public void handle(final RoutingContext context, final MessageStore messageStore) {
        log.traceEntry(() -> context, () -> messageStore);
        String messageID = context.pathParam("message_id");
        if (messageStore.hasUUID(messageID)) {
            log.info(String.format("Found entry for uuid '%s'", messageID));
            context.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(new MessageResponse(messageStore.getMessage(messageID))
                    .toJson()
                    .encodePrettily());
        } else {
            String errorMessage = String.format("Could not find entry for uuid '%s'", messageID);
            log.error(errorMessage);
            context.fail(404, new Exception(errorMessage));
        }
        log.traceExit();
    }
}
