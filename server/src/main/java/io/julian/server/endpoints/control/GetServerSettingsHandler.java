package io.julian.server.endpoints.control;

import io.julian.server.components.Controller;
import io.julian.server.models.control.ServerSettings;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetServerSettingsHandler {
    private static final Logger log = LogManager.getLogger(GetServerSettingsHandler.class.getName());

    public void handle(final RoutingContext routingContext, final Controller controller) {
        log.traceEntry(() -> routingContext, () -> controller);

        routingContext.response()
            .setStatusCode(200)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(new ServerSettings(controller.getStatus(), controller.getFailureChance()).toJson().encodePrettily());

        log.traceExit();
    }
}
