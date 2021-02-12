package io.julian.server.api;

import io.julian.server.client.RegistryManager;
import io.julian.server.client.ServerClient;
import io.julian.server.components.Controller;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DistributedAlgorithm {
    private static final Logger log = LogManager.getLogger(DistributedAlgorithm.class.getName());
    private final Controller controller;
    private final ServerClient client;
    private final RegistryManager registryManager;

    public DistributedAlgorithm(final Controller controller, final Vertx vertx) {
        this.controller = controller;
        this.client = new ServerClient(vertx);
        this.registryManager = new RegistryManager();
    }

    public abstract void actOnCoordinateMessage();

    public abstract void actOnInitialMessage();

    public CoordinationMessage getCoordinationMessage() {
        log.traceEntry();
        return log.traceExit(controller.getCoordinationMessage());
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

    public <T> T mapMessageFromCoordinateMessageToClass(final CoordinationMessage coordinationMessage, final Class<T> objectClass) throws IllegalArgumentException {
        log.traceEntry(() -> coordinationMessage, () -> objectClass);
        return log.traceExit(coordinationMessage.getMessage().mapTo(objectClass));
    }

    public <T> T mapUserDefinitionFromCoordinateMessageToClass(final CoordinationMessage coordinationMessage, final Class<T> objectClass) throws IllegalArgumentException {
        log.traceEntry(() -> coordinationMessage, () -> objectClass);
        return log.traceExit(coordinationMessage.getDefinition().mapTo(objectClass));
    }
}
