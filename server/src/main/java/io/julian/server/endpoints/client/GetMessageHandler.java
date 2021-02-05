package io.julian.server.endpoints.client;

import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.models.response.MessageResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetMessageHandler extends AbstractServerHandler {
    private static final Logger log = LogManager.getLogger(GetMessageHandler.class);

    public void handle(final RoutingContext context, final ServerComponents components) {
        log.traceEntry(() -> context, () -> components);
        String messageID = context.pathParam("message_id");
        if (components.messageStore.hasUUID(messageID)) {
            log.info(String.format("Found entry for uuid '%s'", messageID));
            context.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(new MessageResponse(components.messageStore.getMessage(messageID))
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
