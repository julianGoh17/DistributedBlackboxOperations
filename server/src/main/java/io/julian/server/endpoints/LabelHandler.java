package io.julian.server.endpoints;

import io.julian.server.components.Controller;
import io.julian.server.models.response.LabelResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class LabelHandler {
    private static final Logger log = LogManager.getLogger(LabelHandler.class);

    public void handle(final RoutingContext context, final Controller controller) {
        log.traceEntry(() -> context, () -> controller);
        final String label = Optional.ofNullable(context.queryParam("label"))
            .map(params -> params.get(0))
            .orElse("");

        controller.setLabel(label);
        context.response()
            .setStatusCode(202)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(new LabelResponse(label).toJson().encodePrettily());
        log.traceExit();
    }
}
