package io.julian.client.metrics;

import io.julian.client.model.RequestMethod;
import io.julian.client.model.operation.OverviewComparison;
import io.julian.client.model.response.MismatchedResponse;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Reporter {
    public final static String REPORT_FILE_NAME = "generated-report.txt";

    private final static Logger log = LogManager.getLogger(Reporter.class.getName());
    private final static String HEADER_SEPARATOR_CHAR = "-";

    public Future<Void> createReportFile(final List<MismatchedResponse> responses, final List<OverviewComparison> comparisons, final GeneralMetrics metrics, final String reportFileLocation, final Vertx vertx) {
        log.traceEntry(() -> responses, () -> comparisons, () -> metrics, () -> reportFileLocation, () -> vertx);
        log.info(String.format("%s attempting to create report at location '%s'", Reporter.class.getSimpleName(), reportFileLocation));
        Promise<Void> completedWrite = Promise.promise();
        vertx.fileSystem().writeFile(String.format("%s/%s", reportFileLocation, REPORT_FILE_NAME),
            Buffer.buffer(getReport(responses, comparisons, metrics).toString()), res -> {
                if (res.succeeded()) {
                    log.info(String.format("%s successfully created report", Reporter.class.getSimpleName()));
                    completedWrite.complete();
                } else {
                    log.error(String.format("%s failed to create report because: %s", Reporter.class.getName(), res.cause()));
                    completedWrite.fail(res.cause());
                }
            });
        return log.traceExit(completedWrite.future());
    }

    public void checkReportFolderExists(final String reportFolderLocation) throws FileNotFoundException {
        log.traceEntry(() -> reportFolderLocation);
        log.info(String.format("Checking to see if report folder exists at '%s'", reportFolderLocation));
        File reportFolder = new File(reportFolderLocation);
        if (!reportFolder.exists()) {
            log.error(String.format("Report folder does not exist at '%s'", reportFolderLocation));
            throw new FileNotFoundException(String.format("Could not find folder at '%s'", reportFolder));
        } else if (!reportFolder.isDirectory()) {
            log.error(String.format("File exists at '%s' instead of folder", reportFolderLocation));
            throw new FileNotFoundException(String.format("Not a folder found at '%s'", reportFolder));
        }
        log.info(String.format("Report folder does exist at '%s'", reportFolderLocation));
        log.traceExit();
    }

    // Exposed For Testing
    public StringBuilder getReport(final List<MismatchedResponse> responses, final List<OverviewComparison> comparisons, final GeneralMetrics metrics) {
        log.traceEntry(() -> responses, () -> comparisons, () -> metrics);
        final StringBuilder builder = new StringBuilder();

        log.info("Getting Report");
        getGeneralStatistics(metrics, builder);
        builder.append("\n");
        getGeneralStatistics(metrics, RequestMethod.GET, builder);
        builder.append("\n");
        getGeneralStatistics(metrics, RequestMethod.POST, builder);
        builder.append("\n");
        getGeneralStatistics(metrics, RequestMethod.DELETE, builder);
        builder.append("\n");

        Optional.ofNullable(responses)
            .orElse(Collections.emptyList())
            .forEach(response -> createMismatchedResponseEntry(response, builder));
        builder.append("\n");

        Optional.ofNullable(comparisons)
            .orElse(Collections.emptyList())
            .forEach(response -> createOverviewComparisonEntry(response, builder));

        return log.traceExit(builder);
    }

    // Exposed For Testing
    public void createMismatchedResponseEntry(final MismatchedResponse response, final StringBuilder builder) {
        log.traceEntry(() -> response, () -> builder);
        log.info(String.format("Creating mismatched response for '%s'", response.getMethod()));
        String header = String.format("Mismatched Response For %s Request\n", response.getMethod().toString());
        builder
            .append(header)
            .append(getHeaderSeparator(header))
            .append(String.format("Message Number: %d\n", response.getMessageNumber()))
            .append(String.format("Expected Status Code: %d\n", response.getExpectedStatusCode()))
            .append(String.format("Actual Status Code: %d\n", response.getActualStatusCode()))
            .append(String.format("Error: %s\n", response.getError()));
        log.traceExit();
    }

    // Exposed For Testing
    public void createOverviewComparisonEntry(final OverviewComparison comparison, final StringBuilder builder) {
        log.traceEntry(() -> comparison, () -> builder);
        log.info(String.format("Creating overview comparison for '%s:%d' at %s", comparison.getHost(), comparison.getPort(), comparison.getTimestamp()));
        String header = String.format("Overview Comparison For '%s:%d' at %s\n", comparison.getHost(), comparison.getPort(), comparison.getTimestamp());
        builder
            .append(header)
            .append(getHeaderSeparator(header))
            .append("Missing Expected IDs In Server\n");

        for (final String clientId : comparison.getMissingIdsInServer()) {
            builder.append(String.format("- %s\n", clientId));
        }

        builder.append("Unexpected IDs In Server\n");
        for (final String serverIds : comparison.getMissingIdsInClient()) {
            builder.append(String.format("- %s\n", serverIds));
        }
        log.traceExit();
    }

    // Exposed For Testing
    public void getGeneralStatistics(final GeneralMetrics general, final RequestMethod method, final StringBuilder builder) {
        log.traceEntry(() -> general, () -> method, () -> builder);
        log.info(String.format("Getting general statistics for method '%s'", method));
        String header = String.format("General Statistics For %s:\n", method.toString());
        builder
            .append(header)
            .append(getHeaderSeparator(header))
            .append(String.format("Successful Requests: %d\n", general.getSucceeded(method)))
            .append(String.format("Failed Requests: %d\n", general.getFailed(method)));

        log.traceExit();
    }

    // Exposed For Testing
    public void getGeneralStatistics(final GeneralMetrics general, final StringBuilder builder) {
        log.traceEntry(() -> general, () -> builder);
        log.info("Getting general statistics for method '%s'");
        String header = "General Statistics:\n";
        builder
            .append(header)
            .append(getHeaderSeparator(header))
            .append(String.format("Total Successful Requests: %d\n", general.getSucceeded()))
            .append(String.format("Total Failed Requests: %d\n", general.getFailed()));
        log.traceExit();
    }

    public String getHeaderSeparator(final String header) {
        log.traceEntry(() -> header);
        String separator = HEADER_SEPARATOR_CHAR.repeat(header.length() - 1);

        // Cleans up log
        log.traceExit(separator);
        return separator + "\n";
    }
}
