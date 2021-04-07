package io.julian.metrics.collector.report;

import io.julian.metrics.collector.report.filters.GenericStatistics;
import io.julian.metrics.collector.tracking.MessageStatus;
import io.julian.metrics.collector.tracking.StatusTracker;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

public class ReportCreator {
    private final static Logger log = LogManager.getLogger(ReportCreator.class);
    private final StatusTracker tracker;
    private final Vertx vertx;
    private final GenericStatistics genericStatisticsFilter = new GenericStatistics();

    public final static String GENERAL_STATISTIC_FILTER_NAME = "general";
    public final static String REPORT_FILE_NAME = "metrics-collector-report.txt";

    public ReportCreator(final StatusTracker tracker, final Vertx vertx) {
        this.tracker = tracker;
        this.vertx = vertx;
    }

    public String createReport(final String filterName) {
        log.traceEntry(() -> filterName);
        ReportStringBuilder builder = new ReportStringBuilder();
        log.info(String.format("Creating report with filter '%s'", filterName));
        Function<MessageStatus, String> filter = getFilter(filterName);
        tracker.getStatuses().keys().asIterator().forEachRemaining(messageId -> {
            builder.appendLine(messageId);
            builder.appendLine("-".repeat(messageId.length()));
            builder.append(filter.apply(tracker.getStatuses().get(messageId)));
        });

        return log.traceExit(builder.toString());
    }

    public Future<Void> createReportFile(final String filterName, final String reportLocation) {
        log.traceEntry(() -> filterName, () -> reportLocation);
        Promise<Void> write = Promise.promise();
        log.info(String.format("Creating report file at '%s'", reportLocation));
        vertx.fileSystem().writeFile(String.format("%s/%s", reportLocation, REPORT_FILE_NAME), Buffer.buffer(createReport(filterName)), res -> {
            if (res.succeeded()) {
                log.info(String.format("Succeeded creating report at '%s/%s'", reportLocation, REPORT_FILE_NAME));
                write.complete();
            } else {
                log.info(String.format("Failed creating report at '%s/%s'", reportLocation, REPORT_FILE_NAME));
                log.error(res.cause());
                write.fail(res.cause());
            }
        });
        return log.traceExit(write.future());
    }

    public Function<MessageStatus, String> getFilter(final String filterName) {
        log.traceEntry(() -> filterName);
        switch (filterName) {
            case GENERAL_STATISTIC_FILTER_NAME:
                return log.traceExit(genericStatisticsFilter::toReportEntry);
            default:
                return log.traceExit(genericStatisticsFilter::toReportEntry);
        }
    }
}
