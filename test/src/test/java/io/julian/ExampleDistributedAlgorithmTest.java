package io.julian;

import io.julian.server.components.Controller;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ExampleDistributedAlgorithmTest {
    private final static CoordinationMessage MESSAGE = new CoordinationMessage(
        new CoordinationMetadata("test-id", HTTPRequest.GET),
        new JsonObject(),
        new JsonObject());

    private Vertx vertx;

    @Before
    public void before() {
        vertx = Vertx.vertx();
    }

    @After
    public void after() {
        vertx.close();
    }

    @Test
    public void TestCanIncrementAcceptedMessages() {
        Controller controller = new Controller();
        ExampleDistributedAlgorithm example = new ExampleDistributedAlgorithm(controller, vertx);
        int messages = 5;
        for (int i = 0; i < messages; i++) {
            controller.addToCoordinationQueue(MESSAGE);
        }

        for (int i = 0; i < messages; i++) {
            Assert.assertEquals(messages + i, example.getMessagesInController());
            example.actOnCoordinateMessage();
        }

        Assert.assertEquals(messages, example.getAcceptedMessages());
        Assert.assertEquals(2 * messages, example.getMessagesInController());
    }
}
