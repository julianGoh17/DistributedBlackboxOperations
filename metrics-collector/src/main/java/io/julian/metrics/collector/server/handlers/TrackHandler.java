package io.julian.metrics.collector.server.handlers;

import io.julian.metrics.collector.models.SuccessResponse;
import io.julian.metrics.collector.models.TrackedMessage;
import io.julian.metrics.collector.tracking.StatusTracker;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class TrackHandler {
    private final static Logger log = LogManager.getLogger(TrackHandler.class);
    private final StatusTracker tracker;

    public TrackHandler(final StatusTracker tracker) {
        this.tracker = tracker;
    }

    public void handle(final RoutingContext context) {
        log.traceEntry();
        TrackedMessage message = Optional.ofNullable(context.getBodyAsJson())
            .map(json -> json.mapTo(TrackedMessage.class))
            .orElse(new TrackedMessage(0, "empty", 0));

        tracker.updateStatus(message);

        context
            .response()
            .setStatusCode(200)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(new SuccessResponse(200).toJson().encodePrettily());

        log.traceExit();
    }
}
