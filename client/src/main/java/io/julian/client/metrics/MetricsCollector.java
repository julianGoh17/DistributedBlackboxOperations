package io.julian.client.metrics;

import io.julian.client.model.operation.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class MetricsCollector {
    private static final Logger log = LogManager.getLogger(MetricsCollector.class.getName());

    private final AtomicInteger succeeded;
    private final AtomicInteger failed;
    private final AtomicInteger totalMessages;

    public MetricsCollector() {
        succeeded = new AtomicInteger(0);
        failed = new AtomicInteger(0);
        totalMessages = new AtomicInteger(0);
    }

    public void addMetric(final Operation operation, final boolean hasSucceeded) {
        log.traceEntry(() -> operation, () -> hasSucceeded);
        incrementTotalMessages();
        if (hasSucceeded) {
            succeeded.getAndIncrement();
        } else {
            failed.getAndIncrement();
        }
        log.traceExit();
    }

    private void incrementTotalMessages() {
        log.traceEntry();
        totalMessages.getAndIncrement();
        log.traceExit();
    }

    public int getTotalMessages() {
        log.traceEntry();
        return log.traceExit(totalMessages.get());
    }

    public int getSucceeded() {
        log.traceEntry();
        return log.traceExit(succeeded.get());
    }

    public int getFailed() {
        log.traceEntry();
        return log.traceExit(failed.get());
    }
}
