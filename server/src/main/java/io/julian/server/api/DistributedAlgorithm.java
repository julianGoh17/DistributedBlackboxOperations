package io.julian.server.api;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.api.exceptions.NoIDException;
import io.julian.server.api.exceptions.SameIDException;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DistributedAlgorithm {
    private static final Logger log = LogManager.getLogger(DistributedAlgorithm.class.getName());
    private final Controller controller;
    private final ServerClient client;
    private final MessageStore messageStore;
    private final RegistryManager registryManager;

    public DistributedAlgorithm(final Controller controller, final MessageStore messageStore, final Vertx vertx) {
        this.controller = controller;
        this.messageStore = messageStore;
        this.client = new ServerClient(vertx);
        this.registryManager = new RegistryManager();
    }

    public abstract void actOnCoordinateMessage();

    public abstract void actOnInitialMessage();

    public CoordinationMessage getCoordinationMessage() {
        log.traceEntry();
        return log.traceExit(controller.getCoordinationMessage());
    }

    public void addMessageToServer(final CoordinationMessage message) throws SameIDException {
        log.traceEntry(() -> message);
        log.info(String.format("Attempting to add message with id '%s' to server", message.getMetadata().getMessageID()));
        if (messageStore.hasUUID(message.getMetadata().getMessageID())) {
            SameIDException exception = new SameIDException(message.getMetadata().getMessageID());
            log.error(String.format("Failed to add message with id '%s' to server because: %s",  message.getMetadata().getMessageID(), exception));
            throw exception;
        }
        log.info(String.format("Adding message with id '%s' to server", message.getMetadata().getMessageID()));
        messageStore.putMessage(message.getMetadata().getMessageID(), message.getMessage());
        log.traceExit();
    }

    public void deleteMessageFromServer(final CoordinationMessage message) throws NoIDException {
        log.traceEntry(() -> message);
        log.info(String.format("Attempting to delete message with id '%s' from server", message.getMetadata().getMessageID()));
        if (!messageStore.hasUUID(message.getMetadata().getMessageID())) {
            NoIDException exception = new NoIDException(message.getMetadata().getMessageID());
            log.error(String.format("Failed to delete message with id '%s' from server because: %s", message.getMetadata().getMessageID(), exception));
            throw exception;
        }
        log.info(String.format("Deleting message with id '%s' from server", message.getMetadata().getMessageID()));
        messageStore.removeMessage(message.getMetadata().getMessageID());
        log.traceExit();
    }

    public Controller getController() {
        log.traceEntry();
        return log.traceExit(controller);
    }

    public ServerClient getClient() {
        log.traceEntry();
        return log.traceExit(client);
    }

    public RegistryManager getRegistryManager() {
        log.traceEntry();
        return log.traceExit(registryManager);
    }

    public MessageStore getMessageStore() {
        log.traceEntry();
        return log.traceExit(messageStore);
    }

    public <T> T mapMessageFromCoordinateMessageToClass(final CoordinationMessage coordinationMessage, final Class<T> objectClass) throws IllegalArgumentException {
        log.traceEntry(() -> coordinationMessage, () -> objectClass);
        return log.traceExit(coordinationMessage.getMessage().mapTo(objectClass));
    }

    public <T> T mapUserDefinitionFromCoordinateMessageToClass(final CoordinationMessage coordinationMessage, final Class<T> objectClass) throws IllegalArgumentException {
        log.traceEntry(() -> coordinationMessage, () -> objectClass);
        return log.traceExit(coordinationMessage.getDefinition().mapTo(objectClass));
    }
}
