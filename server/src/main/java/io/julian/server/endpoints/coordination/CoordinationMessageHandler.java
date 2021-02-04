package io.julian.server.endpoints.coordination;

import io.julian.server.api.DistributedAlgorithmVerticle;
import io.julian.server.components.Controller;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CoordinationMessageHandler {
    private static final Logger log = LogManager.getLogger(CoordinationMessageHandler.class);

    public void handle(final RoutingContext context, final Controller controller, final Vertx vertx) {
        log.traceEntry(() -> context, () -> controller, () -> vertx);
        controller.addToQueue(CoordinationMessage.fromJson(context.getBodyAsJson()));

        vertx.eventBus().send(DistributedAlgorithmVerticle.formatAddress(DistributedAlgorithmVerticle.CONSUME_MESSAGE_POSTFIX), "random-message");

        context.response()
            .setStatusCode(200)
            .end();
        log.traceExit();
    }
}
