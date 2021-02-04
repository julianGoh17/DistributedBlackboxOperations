package io.julian.server.components;

import io.julian.server.models.ServerStatus;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class Controller {
    public static final Logger log = LogManager.getLogger(Controller.class);
    public static final ServerStatus DEFAULT_SERVER_STATUS = ServerStatus.AVAILABLE;
    public static final String DEFAULT_LABEL = "";

    private final AtomicReference<ServerStatus> status = new AtomicReference<>(DEFAULT_SERVER_STATUS);
    private final AtomicReference<String> label = new AtomicReference<>(DEFAULT_LABEL);
    private final ConcurrentLinkedQueue<CoordinationMessage> coordinationMessages = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<JsonObject> initialPostMessages = new ConcurrentLinkedQueue<>();

    public void setStatus(final ServerStatus newStatus) {
        log.traceEntry(() -> status);
        this.status.set(newStatus);
        log.traceExit();
    }

    public ServerStatus getStatus() {
        log.traceEntry();
        return log.traceExit(status.get());
    }

    public void setLabel(final String label) {
        log.traceEntry(() -> label);
        this.label.set(label);
        log.traceExit();
    }

    public String getLabel() {
        log.traceEntry();
        return log.traceExit(label.get());
    }

    public void addToCoordinationQueue(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        coordinationMessages.add(message);
        log.traceExit();
    }

    public int getNumberOfCoordinationMessages() {
        log.traceEntry();
        return log.traceExit(coordinationMessages.size());
    }

    public CoordinationMessage getCoordinationMessage() {
        log.traceEntry();
        return log.traceExit(coordinationMessages.poll());
    }

    public void addToInitialPostMessageQueue(final JsonObject message) {
        log.traceEntry(() -> message);
        initialPostMessages.add(message);
        log.traceExit();
    }


    public int getNumberOfInitialPostMessages() {
        log.traceEntry();
        return log.traceExit(initialPostMessages.size());
    }

    public JsonObject getInitialPostMessage() {
        log.traceEntry();
        return log.traceExit(initialPostMessages.poll());
    }
}
