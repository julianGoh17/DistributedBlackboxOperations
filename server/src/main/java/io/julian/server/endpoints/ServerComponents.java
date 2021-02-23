package io.julian.server.endpoints;

import io.julian.server.api.DistributedAlgorithmVerticle;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.vertx.core.Vertx;

public class ServerComponents {
    public final MessageStore messageStore;
    public final Controller controller;
    public final Vertx vertx;
    public final DistributedAlgorithmVerticle verticle;

    public ServerComponents(final MessageStore messageStore, final Controller controller, final Vertx vertx, final DistributedAlgorithmVerticle verticle) {
        this.messageStore = messageStore;
        this.controller = controller;
        this.vertx = vertx;
        this.verticle = verticle;
    }
}
