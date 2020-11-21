package io.julian.server.api;

import io.julian.server.components.Controller;

public interface DistributedAlgorithm {
    default void run(final Controller controller) {
        consumeMessage(controller);
    }

    void consumeMessage(final Controller controller);
}
