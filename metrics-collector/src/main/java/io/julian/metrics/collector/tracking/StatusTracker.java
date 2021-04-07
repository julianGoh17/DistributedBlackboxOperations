package io.julian.metrics.collector.tracking;

import io.julian.metrics.collector.models.TrackedMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class StatusTracker {
    private final static Logger log = LogManager.getLogger(StatusTracker.class);
    private final ConcurrentHashMap<String, MessageStatus> statuses = new ConcurrentHashMap<>();

    public void updateStatus(final TrackedMessage message) {
        log.traceEntry(() -> message);
        if (statuses.containsKey(message.getMessageId())) {
            log.info(String.format("Updating tracked entry for '%s'", message.getMessageId()));
            statuses.get(message.getMessageId()).addTrackedMessage(message);
        } else {
            log.info(String.format("Creating tracked entry for '%s'", message.getMessageId()));
            final MessageStatus status = new MessageStatus();
            status.addTrackedMessage(message);
            statuses.put(message.getMessageId(), status);
        }
        log.traceExit();
    }

    public ConcurrentHashMap<String, MessageStatus> getStatuses() {
        log.traceEntry();
        return log.traceExit(statuses);
    }
}
