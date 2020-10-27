package endpoints;

import components.MessageStore;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import models.MessageIDResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static endpoints.PostMessageHandler.MESSAGE_KEY;

public class PutMessageHandler {
    private static final Logger log = LogManager.getLogger(PutMessageHandler.class);

    public void handle(final RoutingContext context, final MessageStore messageStore) {
        log.traceEntry(() -> context, () -> messageStore);
        String messageID = context.pathParam("message_id");
        JsonObject message = context.getBodyAsJson();

        if (messageStore.hasUUID(messageID)) {
            log.info(String.format("Found entry for uuid '%s', updating message.", messageID));
            messageStore.putMessage(messageID, Optional.ofNullable(message)
                .map(mes -> mes.getJsonObject(MESSAGE_KEY))
                .orElse(new JsonObject()));

            context.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(new MessageIDResponse(messageID).toJson().encodePrettily());
        } else {
            String errorMessage = String.format("Could not find entry for uuid '%s'", messageID);
            log.error(errorMessage);
            context.fail(404, new Exception(errorMessage));
        }

        log.traceExit();
    }
}
