package io.julian.server.api;

import io.julian.server.components.Controller;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.server.models.coordination.CoordinationTimestamp;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class DistributedAlgorithmTest {
    public static final CoordinationMessage TEST_MESSAGE = new CoordinationMessage(new CoordinationMetadata("random-id", new CoordinationTimestamp(LocalDateTime.now())), new JsonObject(), new JsonObject());
    public static final JsonObject TEST_POST_MESSAGE = new JsonObject().put("test", "message");

    public static class ExampleAlgorithm extends DistributedAlgorithm {
        public ExampleAlgorithm(final Controller controller) {
            super(controller);
        }

        @Override
        public void actOnCoordinateMessage() {
            getController().addToCoordinationQueue(TEST_MESSAGE);
        }

        @Override
        public void actOnInitialMessage() {
            getController().addToInitialPostMessageQueue(TEST_POST_MESSAGE);
        }
    }

    @Test
    public void TestExampleAlgorithmActOnCoordinationMessage() {
        Controller controller = new Controller();
        ExampleAlgorithm algorithm = new ExampleAlgorithm(controller);
        int messages = 5;
        for (int i = 0; i < messages; i++) {
            algorithm.actOnCoordinateMessage();
        }

        Assert.assertEquals(messages, controller.getNumberOfCoordinationMessages());
        while (controller.getNumberOfCoordinationMessages() > 0) {
            CoordinationMessage message = controller.getCoordinationMessage();

            Assert.assertEquals(TEST_MESSAGE.getDefinition().encodePrettily(), message.getDefinition().encodePrettily());
            Assert.assertEquals(TEST_MESSAGE.getMessage().encodePrettily(), message.getMessage().encodePrettily());
            Assert.assertEquals(TEST_MESSAGE.getMetadata().getFromServerId(), message.getMetadata().getFromServerId());
            Assert.assertEquals(TEST_MESSAGE.getMetadata().getTimestamp().toValue(), message.getMetadata().getTimestamp().toValue());
        }
    }

    @Test
    public void TestExampleAlgorithmActOnInitialPostMessage() {
        Controller controller = new Controller();
        ExampleAlgorithm algorithm = new ExampleAlgorithm(controller);
        int messages = 5;
        for (int i = 0; i < messages; i++) {
            algorithm.actOnInitialMessage();
        }

        Assert.assertEquals(messages, controller.getNumberOfInitialPostMessages());
        while (controller.getNumberOfCoordinationMessages() > 0) {
            JsonObject message = controller.getInitialPostMessage();
            Assert.assertEquals(message.getString("test"), "message");
        }
    }
}
