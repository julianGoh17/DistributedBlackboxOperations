package io.julian.client.metrics;

import io.julian.client.model.MismatchedResponse;
import io.julian.client.model.RequestMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Reporter {
    private final static Logger log = LogManager.getLogger(Reporter.class.getName());
    private final static String HEADER_SEPARATOR_CHAR = "-";

    // Exposed For Testing
    public StringBuilder createReport(final List<MismatchedResponse> responses, final GeneralMetrics metrics) {
        log.traceEntry(() -> responses, () -> metrics);
        final StringBuilder builder = new StringBuilder();

        getGeneralStatistics(metrics, builder);
        builder.append("\n");
        getGeneralStatistics(metrics, RequestMethod.GET, builder);
        builder.append("\n");
        getGeneralStatistics(metrics, RequestMethod.POST, builder);
        builder.append("\n");
        getGeneralStatistics(metrics, RequestMethod.PUT, builder);
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
