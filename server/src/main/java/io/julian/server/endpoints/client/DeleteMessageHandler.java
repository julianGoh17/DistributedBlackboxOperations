package io.julian.server.endpoints.client;

import io.julian.server.api.DistributedAlgorithmVerticle;
import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
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
        log.info(String.format("%s deleting message with uuid '%s' from server", DeleteMessageHandler.class.getSimpleName(), messageID));

        if (components.messageStore.hasUUID(messageID) && messageID != null) {
            if (components.controller.getConfiguration().doesProcessRequest()) {
                components.messageStore.removeMessage(messageID);
                log.info(String.format("%s successfully removed message with uuid '%s' from server", DeleteMessageHandler.class.getSimpleName(), messageID));
            } else {
                log.info(String.format("%s skipping removing message with uuid '%s' from server as not processing requests", DeleteMessageHandler.class.getSimpleName(), messageID));
            }
            sendResponseBack(context, 200, new MessageIDResponse(messageID).toJson());
            if (components.verticle != null) {
                components.controller.addToClientMessageQueue(new ClientMessage(HTTPRequest.DELETE, null, messageID));
                components.vertx.eventBus().send(
                    components.verticle.formatAddress(DistributedAlgorithmVerticle.CLIENT_MESSAGE_POSTFIX),
                    "");
            } else {
                log.info("Skipping adding to client queue as distributed algorithm not loaded");
            }
        } else {
            log.error(String.format("%s failed to remove message with uuid '%s' from server", DeleteMessageHandler.class.getSimpleName(), messageID));
            context.fail(404, new Exception(String.format(ERROR_RESPONSE, messageID)));
        }

        log.traceExit();
    }
}
