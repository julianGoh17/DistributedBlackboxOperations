package io.julian.server.endpoints.control;

import io.julian.server.components.Controller;
import io.julian.server.models.ServerStatus;
import io.julian.server.models.control.ServerSettings;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SetServerSettingsHandler {
    private static final Logger log = LogManager.getLogger(SetServerSettingsHandler.class.getName());

    public void handle(final RoutingContext routingContext, final Controller controller) {
        log.traceEntry(() -> routingContext, () -> controller);
        log.info(String.format("%s updating ServerSettings", GetServerSettingsHandler.class.getSimpleName()));
        final ServerSettings settings = routingContext.getBodyAsJson().mapTo(ServerSettings.class);

        if (settings.getStatus() != ServerStatus.UNKNOWN) {
            controller.setStatus(settings.getStatus());
        }
        if (settings.getFailureChance() != null) {
            controller.setFailureChance(settings.getFailureChance());
        }

        routingContext.response()
            .setStatusCode(202)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(settings.toJson().encodePrettily());

        log.traceExit();
    }
}
