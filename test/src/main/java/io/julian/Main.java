package io.julian;

import io.julian.server.api.DistributedAlgorithm;
import io.julian.server.components.Controller;
import io.julian.server.models.coordination.CoordinationMessage;

import java.util.concurrent.atomic.AtomicInteger;

public class Main extends DistributedAlgorithm {
    private final AtomicInteger acceptedMessages = new AtomicInteger(0);

    public Main(final Controller controller) {
        super(controller);
    }

    public void consumeMessage() {
        CoordinationMessage message = getController().getCoordinationMessage();
        getController().addToQueue(message);
        getController().addToQueue(message);
        acceptedMessages.getAndIncrement();
    }

    public int getAcceptedMessages() {
        return acceptedMessages.get();
    }

    public int getMessagesInController() {
        return getController().getNumberOfCoordinationMessages();
    }
}
