package io.julian.server.endpoints;

import io.julian.server.components.Controller;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CoordinationMessageHandler {
    private static final Logger log = LogManager.getLogger(CoordinationMessageHandler.class);

    public void handle(final RoutingContext context, final Controller controller) {
        log.traceEntry(() -> context, () -> controller);
        controller.addToQueue(CoordinationMessage.fromJson(context.getBodyAsJson()));

        // TODO: need to add vertx.send message to the Distributed algorith verticle

        context.response()
            .setStatusCode(200)
            .end();
        log.traceExit();
    }
}
