package io.julian.server.endpoints.client;

import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.models.response.MessageIDResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class DeleteMessageHandler extends AbstractServerHandler  {
    private final static Logger log = LogManager.getLogger(DeleteMessageHandler.class.getName());
    public final static String ERROR_RESPONSE = "Couldn't delete message with uuid '%s' from server";

    @Override
    protected void handle(final RoutingContext context, final ServerComponents components) {
        log.traceEntry(() -> components, () -> components);
        String messageID = Optional.ofNullable(context.queryParam("messageId"))
            .map(params -> params.get(0))
            .orElse(null);

        if (components.messageStore.hasUUID(messageID) && messageID != null) {
            components.messageStore.removeMessage(messageID);
            sendResponseBack(context, 200, new MessageIDResponse(messageID).toJson());
        } else {
            String errorMessage = String.format(ERROR_RESPONSE, messageID);
            log.error(errorMessage);
            context.fail(404, new Exception(errorMessage));
        }

        log.traceExit();
    }
}
