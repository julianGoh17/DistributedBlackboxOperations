package io.julian;

import io.julian.server.components.Controller;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.server.models.coordination.CoordinationTimestamp;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class MainTest {
    private final static CoordinationMessage MESSAGE = new CoordinationMessage(
        new CoordinationMetadata("test-id", new CoordinationTimestamp(LocalDateTime.now())),
        new JsonObject(),
        new JsonObject());

    @Test
    public void TestCanIncrementAcceptedMessages() {
        Controller controller = new Controller();
        Main main = new Main(controller);
        int messages = 5;
        for (int i = 0; i < messages; i++) {
            controller.addToQueue(MESSAGE);
        }

        for (int i = 0; i < messages; i++) {
            Assert.assertEquals(messages + i, main.getMessagesInController());
            main.consumeMessage();
        }

        Assert.assertEquals(messages, main.getAcceptedMessages());
        Assert.assertEquals(2 * messages, main.getMessagesInController());
    }
}
