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

    public static class ExampleAlgorithm implements DistributedAlgorithm {
        @Override
        public void consumeMessage(final Controller controller) {
            controller.addToQueue(TEST_MESSAGE);
        }
    }

    @Test
    public void TestExampleAlgorithmCanUseRunMethod() {
        Controller controller = new Controller();
        ExampleAlgorithm algorithm = new ExampleAlgorithm();
        int messages = 5;
        for (int i = 0; i < messages; i++) {
            algorithm.consumeMessage(controller);
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
}
