package io.julian.server.endpoints.coordination;

import io.julian.server.components.Controller;
import io.julian.server.models.response.SetStatusResponse;
import io.julian.server.models.ServerStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class SetStatusHandler {
    private static final Logger log = LogManager.getLogger(SetStatusHandler.class.getName());

    public void handle(final RoutingContext routingContext, final Controller controller) {
        log.traceEntry(() -> routingContext, () -> controller);
        final ServerStatus serverStatus = Optional.ofNullable(routingContext.queryParam("status"))
            .map(params -> params.get(0))
            .map(str -> {
                ServerStatus status = ServerStatus.forValue(str);
                if (ServerStatus.UNKNOWN == status) {
                    log.info("Unknown enum detected, defaulting to available");
                    status = ServerStatus.AVAILABLE;
                }
                return status;
            })
            .orElse(ServerStatus.AVAILABLE);

        controller.setStatus(serverStatus);
        routingContext.response()
            .setStatusCode(202)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(new SetStatusResponse(serverStatus).toJson().encodePrettily());

        log.traceExit();
    }
}
