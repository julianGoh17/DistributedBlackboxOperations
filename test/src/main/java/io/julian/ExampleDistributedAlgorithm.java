package io.julian;

import io.julian.server.api.DistributedAlgorithm;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.Vertx;

import java.util.concurrent.atomic.AtomicInteger;

public class ExampleDistributedAlgorithm extends DistributedAlgorithm {
    private final AtomicInteger acceptedMessages = new AtomicInteger(0);

    public ExampleDistributedAlgorithm(final Controller controller, final MessageStore messageStore, final Vertx vertx) {
        super(controller, messageStore, vertx);
    }

    public void actOnCoordinateMessage() {
        CoordinationMessage message = getController().getCoordinationMessage();
        getController().addToCoordinationQueue(message);
        getController().addToCoordinationQueue(message);
        addToDeadLetterQueue(message);
        acceptedMessages.getAndIncrement();
    }

    @Override
    public void actOnInitialMessage() {}

    public int getAcceptedMessages() {
        return acceptedMessages.get();
    }

    public int getMessagesInController() {
        return getController().getNumberOfCoordinationMessages();
    }
}
