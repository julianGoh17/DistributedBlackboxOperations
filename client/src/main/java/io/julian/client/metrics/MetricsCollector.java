package io.julian.client.metrics;

import io.julian.client.Exception.ClientException;
import io.julian.client.model.MismatchedResponse;
import io.julian.client.model.operation.Operation;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class MetricsCollector {
    private static final Logger log = LogManager.getLogger(MetricsCollector.class.getName());

    private final GeneralMetrics general;
    private final List<MismatchedResponse> mismatchedResponses;

    public MetricsCollector() {
        general = new GeneralMetrics();
        mismatchedResponses = Collections.synchronizedList(new ArrayList<>());
    }

    public void addSucceededMetric(final Operation operation) {
        log.traceEntry(() -> operation);
        general.incrementSuccessMethod(operation.getAction().getMethod());
        log.traceExit();
    }

    public void addFailedMetric(final Operation operation, final ClientException exception) {
        log.traceEntry(() -> operation, () -> exception);
        general.incrementFailedMethod(operation.getAction().getMethod());
        mismatchedResponses.add(new MismatchedResponse(operation, exception));
        log.traceExit();
    }
}
