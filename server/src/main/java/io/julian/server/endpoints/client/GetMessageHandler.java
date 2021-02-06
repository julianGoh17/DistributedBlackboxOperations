package io.julian.server.endpoints.client;

import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.models.response.MessageResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class GetMessageHandler extends AbstractServerHandler {
    private static final Logger log = LogManager.getLogger(GetMessageHandler.class);

    public void handle(final RoutingContext context, final ServerComponents components) {
        log.traceEntry(() -> context, () -> components);
        String messageID = Optional.ofNullable(context.queryParam("messageId"))
            .map(params -> params.get(0))
            .orElse(null);

        if (components.messageStore.hasUUID(messageID)) {
            log.info(String.format("Found entry for uuid '%s'", messageID));
            sendResponseBack(context, 200,
                new MessageResponse(components.messageStore.getMessage(messageID)).toJson());
        } else {
            String errorMessage = String.format("Could not find entry for uuid '%s'", messageID);
            log.error(errorMessage);
            context.fail(404, new Exception(errorMessage));
        }
        log.traceExit();
    }
}
