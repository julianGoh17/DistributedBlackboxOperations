package io.julian.server.endpoints.coordination;

import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.models.response.LabelResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class SetLabelHandler extends AbstractServerHandler {
    private static final Logger log = LogManager.getLogger(SetLabelHandler.class);

    public void handle(final RoutingContext context, final ServerComponents components) {
        log.traceEntry(() -> context, () -> components);
        // Should never get into orElse as OpenAPI validation requires label query string
        final String label = Optional.ofNullable(context.queryParam("label"))
            .map(params -> params.get(0))
            .orElse("");
        log.info(String.format("%s updating server label to '%s'", SetLabelHandler.class.getSimpleName(), label));

        components.controller.setLabel(label);
        sendResponseBack(context, 202, new LabelResponse(label).toJson());
        log.traceExit();
    }
}
