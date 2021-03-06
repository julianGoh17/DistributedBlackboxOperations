package io.julian.metrics.collector.server.handlers;

import io.julian.metrics.collector.models.ErrorResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ErrorHandler {
    private static final Logger log = LogManager.getLogger(ErrorHandler.class.getName());

    public void handle(final RoutingContext context) {
        log.traceEntry(() -> context);
        ErrorResponse error = new ErrorResponse(context.statusCode(), context.failure() != null ? context.failure() : new Exception("Not Found"));
        log.error(error.getError());

        context.response()
            .setStatusCode(context.statusCode())
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(error.toJson().encodePrettily());
        log.traceExit();
    }
}