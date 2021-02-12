package io.julian.server.components;

import io.julian.server.models.ServerStatus;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class Controller {
    public static final Logger log = LogManager.getLogger(Controller.class);
    public static final ServerStatus DEFAULT_SERVER_STATUS = ServerStatus.AVAILABLE;
    public static final String DEFAULT_LABEL = "";
    public static final Float DEFAULT_MESSAGE_FAILURE_CHANCE = 0.4f;

    private final AtomicReference<ServerStatus> status = new AtomicReference<>(DEFAULT_SERVER_STATUS);
    private final AtomicReference<String> label = new AtomicReference<>(DEFAULT_LABEL);
    private final ConcurrentLinkedQueue<CoordinationMessage> coordinationMessages = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<ClientMessage> clientMessages = new ConcurrentLinkedQueue<>();
    private final AtomicReference<Float> failureChance = new AtomicReference<>(DEFAULT_MESSAGE_FAILURE_CHANCE);

    public void setStatus(final ServerStatus newStatus) {
        log.traceEntry(() -> newStatus);
        log.info(String.format("Setting server status to '%s'", newStatus));

        this.status.set(newStatus);
        log.traceExit();
    }

    public ServerStatus getStatus() {
        log.traceEntry();
        return log.traceExit(status.get());
    }

    public void setLabel(final String label) {
        log.traceEntry(() -> label);
        log.info(String.format("Setting server label to '%s'", label));
        this.label.set(label);
        log.traceExit();
    }

    public String getLabel() {
        log.traceEntry();
        return log.traceExit(label.get());
    }

    public void addToCoordinationQueue(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        log.info("Adding message to coordination queue");
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

    public void addToClientMessageQueue(final ClientMessage message) {
        log.traceEntry(() -> message);
        log.info("Adding message to client message queue");
        clientMessages.add(message);
        log.traceExit();
    }

    public int getNumberOfClientMessages() {
        log.traceEntry();
        return log.traceExit(clientMessages.size());
    }

    public ClientMessage getClientMessage() {
        log.traceEntry();
        return log.traceExit(clientMessages.poll());
    }

    public Float getFailureChance() {
        log.traceEntry();
        return log.traceExit(failureChance.get());
    }

    public void setFailureChance(final float failureChance) {
        log.traceEntry(() -> failureChance);
        log.info(String.format("Setting server failure chance to %.2f", failureChance));
        this.failureChance.set(failureChance);
        log.traceExit();
    }
}
