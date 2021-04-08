package io.julian.metrics.collector.tracking;

import io.julian.metrics.collector.models.TrackedMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MessageStatus {
    private final static Logger log = LogManager.getLogger(MessageStatus.class);
    private final AtomicReference<Float> totalMessageSize = new AtomicReference<>(0f);
    private final AtomicInteger failedMessages = new AtomicInteger();
    private final AtomicInteger successfulMessages = new AtomicInteger();

    public void addTrackedMessage(final TrackedMessage message) {
        log.traceEntry(() -> message);
        totalMessageSize.set(totalMessageSize.get() + message.getMessageSize());
        if (message.getStatusCode() == 200) {
            successfulMessages.incrementAndGet();
        } else {
            failedMessages.incrementAndGet();
        }
        log.traceExit();
    }

    public float getTotalMessageSize() {
        log.traceEntry();
        return log.traceExit(totalMessageSize.get());
    }

    public int getFailedMessages() {
        log.traceEntry();
        return log.traceExit(failedMessages.get());
    }

    public int getSuccessfulMessages() {
        log.traceEntry();
        return log.traceExit(successfulMessages.get());
    }
}
