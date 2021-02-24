package io.julian.server.api;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.api.exceptions.NoIDException;
import io.julian.server.api.exceptions.SameIDException;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DistributedAlgorithm {
    private static final Logger log = LogManager.getLogger(DistributedAlgorithm.class.getName());
    protected final Controller controller;
    protected final ServerClient client;
    protected final MessageStore messageStore;
    protected final RegistryManager registryManager;
    protected final Vertx vertx;

    public DistributedAlgorithm(final Controller controller, final MessageStore messageStore, final Vertx vertx) {
        this.controller = controller;
        this.messageStore = messageStore;
        this.client = new ServerClient(vertx);
        this.registryManager = new RegistryManager();
        this.vertx = vertx;
    }

    /**
     * This method gets run every time a coordinate message enters the coordination queue. The user should override this
     * method to tell the server how to interact once they receive a message from another server.
     */
    public abstract void actOnCoordinateMessage();

    /**
     * This method gets run every time they receive a coordinate message from another server, and the coordinate message
     * gets added to the coordination queue. The user should override this method to tell the server how to interact
     * once they receive a message from another server.
     */
    public abstract void actOnInitialMessage();

    /**
     * Retrieve and remove the earliest message from the coordination queue
     * @return the earliest coordination message
     */
    public CoordinationMessage getCoordinationMessage() {
        log.traceEntry();
        return log.traceExit(controller.getCoordinationMessage());
    }

    /**
     * Retrieve and remove the earliest message from the client message queue
     * @return the earliest client message
     */
    public ClientMessage getClientMessageMessage() {
        log.traceEntry();
        return log.traceExit(controller.getClientMessage());
    }

    /**
     * Adds the user message and the id of that message (received from another server) to the server's messages if the
     * ID does not currently exist in the server.
     * @param message Coordination message received from another server.
     * @throws SameIDException If the current server has a message with the same ID as the one in the coordinate message,
     * the server has reached a scenario where the server states are different and will throw an exception for the user to deal with it.
     */
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

    /**
     * Deletes a message from the server with the id stored inside the coordinate message (received from another server) if possible.
     * @param message Coordination message received from another server.
     * @throws NoIDException If the server attempts to delete an ID that does not exist in the server, it will throw this exception.
     */
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


    /**
     * Retrieves the controller of the server, which will give access to the internal server settings
     * @return the server controller
     */
    public Controller getController() {
        log.traceEntry();
        return log.traceExit(controller);
    }

    /**
     * Retrieves the ServerClient who can easily communicate with the other servers
     * @return the server client
     */
    public ServerClient getClient() {
        log.traceEntry();
        return log.traceExit(client);
    }

    /**
     * Retrieves the registry manager, which contains all registered servers and the label of the servers.
     * @return Registry Manager
     */
    public RegistryManager getRegistryManager() {
        log.traceEntry();
        return log.traceExit(registryManager);
    }

    /**
     * Retrieves the message store, which contains all the known client updates.
     * @return message store
     */
    public MessageStore getMessageStore() {
        log.traceEntry();
        return log.traceExit(messageStore);
    }

    /**
     * A helper function that allows the user to map the message JSON to a Java class.
     * @param coordinationMessage The coordinate message to map the message JSON to the Java class
     * @param objectClass The object class to map the JSON to
     * @param <T>  Any class that Jackson knows how to map JSON to a Java class.
     * @return The message JSON as a Java class
     * @throws IllegalArgumentException The exception is thrown when the JSON contains keys that the object class
     * does not know how to map to the class.
     */
    public <T> T mapMessageFromCoordinateMessageToClass(final CoordinationMessage coordinationMessage, final Class<T> objectClass) throws IllegalArgumentException {
        log.traceEntry(() -> coordinationMessage, () -> objectClass);
        return log.traceExit(coordinationMessage.getMessage().mapTo(objectClass));
    }

    /**
     * A helper function that allows the user to map the user definition JSON to a Java class.
     * @param coordinationMessage The coordinate message to map the user definition JSON to the Java class
     * @param objectClass The object class to map the JSON to
     * @param <T> Any class that Jackson knows how to map JSON to a Java class.
     * @return The message JSON as a Java class
     * @throws IllegalArgumentException The exception is thrown when the JSON contains keys that the object class
     * does not know how to map to the class.
     */
    public <T> T mapUserDefinitionFromCoordinateMessageToClass(final CoordinationMessage coordinationMessage, final Class<T> objectClass) throws IllegalArgumentException {
        log.traceEntry(() -> coordinationMessage, () -> objectClass);
        return log.traceExit(coordinationMessage.getDefinition().mapTo(objectClass));
    }
}
