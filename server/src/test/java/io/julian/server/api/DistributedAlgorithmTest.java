package io.julian.server.api;

import io.julian.server.components.Controller;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.server.models.coordination.CoordinationTimestamp;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;

@RunWith(VertxUnitRunner.class)
public class DistributedAlgorithmTest {
    public static final CoordinationMessage TEST_MESSAGE = new CoordinationMessage(new CoordinationMetadata("random-id", new CoordinationTimestamp(LocalDateTime.now()), HTTPRequest.GET, "test", "id"), new JsonObject(), new JsonObject());
    public static final JsonObject TEST_POST_MESSAGE = new JsonObject().put("test", "message");

    private Vertx vertx;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @After
    public void after() {
        vertx.close();
    }

    public static class ExampleAlgorithm extends DistributedAlgorithm {
        public ExampleAlgorithm(final Controller controller, final Vertx vertx) {
            super(controller, vertx);
        }

        @Override
        public void actOnCoordinateMessage() {
            getController().addToCoordinationQueue(TEST_MESSAGE);
        }

        @Override
        public void actOnInitialMessage() {
            getController().addToClientMessageQueue(new ClientMessage(HTTPRequest.POST, TEST_POST_MESSAGE, "test"));
        }
    }

    @Test
    public void TestExampleAlgorithmActOnCoordinationMessage() {
        Controller controller = new Controller();
        ExampleAlgorithm algorithm = new ExampleAlgorithm(controller, vertx);
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
        ExampleAlgorithm algorithm = new ExampleAlgorithm(controller, vertx);
        int messages = 5;
        for (int i = 0; i < messages; i++) {
            algorithm.actOnInitialMessage();
        }

        Assert.assertEquals(messages, controller.getNumberOfClientMessages());
        while (controller.getNumberOfCoordinationMessages() > 0) {
            ClientMessage message = controller.getClientMessage();
            Assert.assertEquals(message.getMessage().getString("test"), "message");
        }
    }
}
