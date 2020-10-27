package endpoints;

import components.MessageStore;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetMessageHandler {
    private static final Logger log = LogManager.getLogger(GetMessageHandler.class);
    public static final String URI = "/client";

    public void handle(final RoutingContext context, final MessageStore messageStore) {
        log.traceEntry(() -> context, () -> messageStore);
        String messageID = context.pathParam("message_id");
        if (messageStore.hasUUID(messageID)) {
            log.info(String.format("Found entry for uuid '%s'", messageID));
            context.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(messageStore.getMessage(messageID).encodePrettily());
        } else {
            log.info(String.format("Could not find entry for uuid '%s'", messageID));
            context.fail(404, new Exception(String.format("Could not find message with id '%s'", messageID)));
        }
        log.traceExit();
    }
}
