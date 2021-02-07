package io.julian.client.metrics;

import io.julian.client.model.response.MismatchedResponse;
import io.julian.client.model.RequestMethod;
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

    public Future<Void> createReportFile(final List<MismatchedResponse> responses, final GeneralMetrics metrics, final String reportFileLocation, final Vertx vertx) {
        log.traceEntry(() -> responses, () -> metrics, () -> reportFileLocation, () -> vertx);
        Promise<Void> completedWrite = Promise.promise();
        vertx.fileSystem().writeFile(String.format("%s/%s", reportFileLocation, REPORT_FILE_NAME),
            Buffer.buffer(getReport(responses, metrics).toString()), res -> {
                if (res.succeeded()) {
                    completedWrite.complete();
                } else {
                    log.error(res.cause());
                    completedWrite.fail(res.cause());
                }
            });
        return log.traceExit(completedWrite.future());
    }

    public void checkReportFolderExists(final String reportFolderLocation) throws FileNotFoundException {
        log.traceEntry(() -> reportFolderLocation);
        File reportFolder = new File(reportFolderLocation);
        if (!reportFolder.exists()) {
            throw new FileNotFoundException(String.format("Could not find folder at '%s'", reportFolder));
        } else if (!reportFolder.isDirectory()) {
            throw new FileNotFoundException(String.format("Not a folder found at '%s'", reportFolder));
        }
        log.traceExit();
    }

    // Exposed For Testing
    public StringBuilder getReport(final List<MismatchedResponse> responses, final GeneralMetrics metrics) {
        log.traceEntry(() -> responses, () -> metrics);
        final StringBuilder builder = new StringBuilder();

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

        return log.traceExit(builder);
    }

    // Exposed For Testing
    public void createMismatchedResponseEntry(final MismatchedResponse response, final StringBuilder builder) {
        log.traceEntry(() -> response, () -> builder);
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
    public void getGeneralStatistics(final GeneralMetrics general, final RequestMethod method, final StringBuilder builder) {
        log.traceEntry(() -> general, () -> method, () -> builder);
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
