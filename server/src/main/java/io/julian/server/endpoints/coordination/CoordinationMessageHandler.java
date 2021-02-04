package io.julian.server.endpoints.coordination;

import io.julian.server.api.DistributedAlgorithmVerticle;
import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CoordinationMessageHandler extends AbstractServerHandler  {
    private static final Logger log = LogManager.getLogger(CoordinationMessageHandler.class);

    public void handle(final RoutingContext context, final ServerComponents components) {
        log.traceEntry(() -> context, () -> components);
        components.controller.addToCoordinationQueue(CoordinationMessage.fromJson(context.getBodyAsJson()));

        components.vertx.eventBus().send(
            DistributedAlgorithmVerticle.formatAddress(DistributedAlgorithmVerticle.COORDINATE_MESSAGE_POSTFIX), "");

        context.response()
            .setStatusCode(200)
            .end();
        log.traceExit();
    }
}
