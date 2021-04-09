package io.julian.client.metrics;

import io.julian.client.exception.ClientException;
import io.julian.client.model.operation.OverviewComparison;
import io.julian.client.model.response.MismatchedResponse;
import io.julian.client.model.operation.Operation;
import io.julian.server.models.response.ServerOverview;
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
    private final List<OverviewComparison> overviewComparisons;

    public MetricsCollector() {
        general = new GeneralMetrics();
        mismatchedResponses = Collections.synchronizedList(new ArrayList<>());
        overviewComparisons = Collections.synchronizedList(new ArrayList<>());
    }

    public void addComparisonCheck(final List<String> clientIds, final ServerOverview overview) {
        log.traceEntry(() -> overview);
        OverviewComparison comparison = new OverviewComparison(overview.getHost(), overview.getPort());
        comparison.compareClientExpectedStateToServerOverview(clientIds, overview.getMessageIds());
        overviewComparisons.add(comparison);
        log.traceExit();
    }

    public void addSucceededMetric(final Operation operation) {
        log.traceEntry(() -> operation);
        log.info(String.format("%s adding succeeded metric", MetricsCollector.class.getSimpleName()));
        general.incrementSuccessMethod(operation.getAction().getMethod());
        log.traceExit();
    }

    public void addFailedMetric(final Operation operation, final ClientException exception) {
        log.traceEntry(() -> operation, () -> exception);
        log.info(String.format("%s adding failed metric", MetricsCollector.class.getSimpleName()));
        general.incrementFailedMethod(operation.getAction().getMethod());
        mismatchedResponses.add(new MismatchedResponse(operation, exception));
        log.traceExit();
    }
}
