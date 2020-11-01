package io.julian.client.metrics;

import io.julian.client.Exception.ClientException;
import io.julian.client.model.MismatchedResponse;
import io.julian.client.model.RequestMethod;
import io.julian.client.model.operation.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MetricsCollector {
    private static final Logger log = LogManager.getLogger(MetricsCollector.class.getName());

    private final AtomicInteger succeededGets;
    private final AtomicInteger failedGets;
    private final AtomicInteger succeededPosts;
    private final AtomicInteger failedPosts;
    private final AtomicInteger succeededPuts;
    private final AtomicInteger failedPuts;

    private final List<MismatchedResponse> mismatchedResponses;

    public MetricsCollector() {
        succeededGets = new AtomicInteger();
        failedGets = new AtomicInteger();
        succeededPosts = new AtomicInteger();
        failedPosts = new AtomicInteger();
        succeededPuts = new AtomicInteger();
        failedPuts = new AtomicInteger();
        mismatchedResponses = Collections.synchronizedList(new ArrayList<>());
    }

    public void addSucceededMetric(final Operation operation) {
        log.traceEntry(() -> operation);
        incrementSuccessMethod(operation.getAction().getMethod());
        log.traceExit();
    }

    private void incrementSuccessMethod(final RequestMethod method) {
        log.traceEntry(() -> method);
        switch (method) {
            case GET:
                succeededGets.getAndIncrement();
                break;
            case POST:
                succeededPosts.getAndIncrement();
                break;
            case PUT:
                succeededPuts.getAndIncrement();
                break;
        }
        log.traceExit();
    }

    public void addFailedMetric(final Operation operation, final ClientException exception) {
        log.traceEntry(() -> operation, () -> exception);
        incrementFailedMethod(operation.getAction().getMethod());
        mismatchedResponses.add(new MismatchedResponse(operation, exception));
        log.traceExit();
    }

    private void incrementFailedMethod(final RequestMethod method) {
        log.traceEntry(() -> method);
        switch (method) {
            case GET:
                failedGets.getAndIncrement();
                break;
            case POST:
                failedPosts.getAndIncrement();
                break;
            case PUT:
                failedPuts.getAndIncrement();
                break;
        }
        log.traceExit();
    }

    public int getTotalMessages() {
        log.traceEntry();
        return log.traceExit(getSucceeded() + getFailed());
    }

    public int getSucceeded() {
        log.traceEntry();
        return log.traceExit(succeededGets.get() + succeededPosts.get() + succeededPuts.get());
    }

    public int getSucceeded(final RequestMethod method) {
        log.traceEntry(() -> method);

        switch (method) {
            case GET:
                return log.traceExit(succeededGets.get());
            case POST:
                return log.traceExit(succeededPosts.get());
            case PUT:
                return log.traceExit(succeededPuts.get());
        }
        return log.traceExit(0);
    }

    public int getFailed() {
        log.traceEntry();
        return log.traceExit(failedGets.get() + failedPosts.get() + failedPuts.get());
    }

    public int getFailed(final RequestMethod method) {
        log.traceEntry(() -> method);

        switch (method) {
            case GET:
                return log.traceExit(failedGets.get());
            case POST:
                return log.traceExit(failedPosts.get());
            case PUT:
                return log.traceExit(failedPuts.get());
        }
        return log.traceExit(0);
    }

    public List<MismatchedResponse> getMismatchedResponses() {
        return mismatchedResponses;
    }
}
