package io.julian.server.endpoints.coordination;

import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.endpoints.client.GetMessageHandler;
import io.julian.server.models.response.LabelResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetLabelHandler extends AbstractServerHandler {
    private static final Logger log = LogManager.getLogger(SetLabelHandler.class);

    @Override
    protected void handle(final RoutingContext context, final ServerComponents components) {
        log.traceEntry(() -> context, () -> components);
        log.info(String.format("%s retrieving server label for sender", GetMessageHandler.class.getSimpleName()));
        sendResponseBack(context, 200, new LabelResponse(components.controller.getLabel()).toJson());
        log.traceExit();
    }
}
