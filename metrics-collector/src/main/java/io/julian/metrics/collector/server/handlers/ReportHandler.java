package io.julian.metrics.collector.server.handlers;

import io.julian.metrics.collector.models.SuccessResponse;
import io.julian.metrics.collector.report.ReportCreator;
import io.julian.metrics.collector.server.Configuration;
import io.julian.metrics.collector.tracking.StatusTracker;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class ReportHandler {
    private final static Logger log = LogManager.getLogger(TrackHandler.class);
    private final ReportCreator creator;
    private final Configuration configuration;

    public ReportHandler(final StatusTracker tracker, final Vertx vertx, final Configuration configuration) {
        this.creator = new ReportCreator(tracker, vertx);
        this.configuration = configuration;
    }

    public void handle(final RoutingContext context) {
        log.traceEntry(() -> context);
        final String filterName = Optional.ofNullable(context.queryParam("filterName"))
            .map(list -> list.get(0))
            .orElse(ReportCreator.GENERAL_STATISTIC_FILTER_NAME);

        creator.createReportFile(filterName, configuration.getReportPath())
            .onComplete(res -> {
                if (res.succeeded()) {
                    context.response()
                        .setStatusCode(200)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .end(new SuccessResponse(200).toJson().encodePrettily());
                } else {
                    context.fail(403, res.cause());
                }
            });

        log.traceExit();
    }
}
