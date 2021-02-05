package io.julian.server.endpoints.coordination;

import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.models.response.LabelResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class LabelHandler extends AbstractServerHandler {
    private static final Logger log = LogManager.getLogger(LabelHandler.class);

    public void handle(final RoutingContext context, final ServerComponents components) {
        log.traceEntry(() -> context, () -> components);
        // Should never get into orElse as OpenAPI validation requires label query string
        final String label = Optional.ofNullable(context.queryParam("label"))
            .map(params -> params.get(0))
            .orElse("");

        components.controller.setLabel(label);
        context.response()
            .setStatusCode(202)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(new LabelResponse(label).toJson().encodePrettily());
        log.traceExit();
    }
}
