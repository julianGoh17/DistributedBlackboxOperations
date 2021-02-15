package io.julian.server.api;

import io.julian.server.api.exceptions.NoIDException;
import io.julian.server.api.exceptions.SameIDException;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
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
    public static final CoordinationMessage TEST_MESSAGE = new CoordinationMessage(
        new CoordinationMetadata("random-id", new CoordinationTimestamp(LocalDateTime.now()), HTTPRequest.GET, "test"),
        new JsonObject().put("some", "random"),
        new JsonObject().put("not", new JsonObject().put("equal", "keys")));
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
        public ExampleAlgorithm(final Controller controller, final MessageStore messageStore, final Vertx vertx) {
            super(controller, messageStore, vertx);
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
        ExampleAlgorithm algorithm = createExampleAlgorithm();
        int messages = 5;
        for (int i = 0; i < messages; i++) {
            algorithm.actOnCoordinateMessage();
        }

        Assert.assertEquals(messages, algorithm.getController().getNumberOfCoordinationMessages());
        while (algorithm.getController().getNumberOfCoordinationMessages() > 0) {
            CoordinationMessage message = algorithm.getController().getCoordinationMessage();

            Assert.assertEquals(TEST_MESSAGE.getDefinition().encodePrettily(), message.getDefinition().encodePrettily());
            Assert.assertEquals(TEST_MESSAGE.getMessage().encodePrettily(), message.getMessage().encodePrettily());
            Assert.assertEquals(TEST_MESSAGE.getMetadata().getFromServerId(), message.getMetadata().getFromServerId());
            Assert.assertEquals(TEST_MESSAGE.getMetadata().getTimestamp().toValue(), message.getMetadata().getTimestamp().toValue());
        }
    }

    @Test
    public void TestExampleAlgorithmActOnInitialPostMessage() {
        ExampleAlgorithm algorithm = createExampleAlgorithm();
        int messages = 5;
        for (int i = 0; i < messages; i++) {
            algorithm.actOnInitialMessage();
        }

        Assert.assertEquals(messages, algorithm.getController().getNumberOfClientMessages());
        while (algorithm.getController().getNumberOfCoordinationMessages() > 0) {
            ClientMessage message = algorithm.getController().getClientMessage();
            Assert.assertEquals(message.getMessage().getString("test"), "message");
        }
    }

    @Test
    public void TestExampleAlgorithmWillThrowErrorWhenIDPresentInMessages() {
        ExampleAlgorithm algorithm = createExampleAlgorithm();
        algorithm.getMessageStore().putMessage(TEST_MESSAGE.getMetadata().getMessageID(), TEST_MESSAGE.getMessage());
        Assert.assertTrue(algorithm.getMessageStore().hasUUID(TEST_MESSAGE.getMetadata().getMessageID()));

        try {
            algorithm.addMessageToServer(TEST_MESSAGE);
            Assert.fail();
        } catch (SameIDException e) {
            Assert.assertEquals(TEST_MESSAGE.getMetadata().getMessageID(), e.getId());
            Assert.assertEquals(String.format("Server already contains message with id '%s'", TEST_MESSAGE.getMetadata().getMessageID()), e.toString());
        }
    }

    @Test
    public void TestExampleAlgorithmDoesNotFailToPutMessageWhenIDDoesNotExistInMessages() {
        ExampleAlgorithm algorithm = createExampleAlgorithm();
        Assert.assertFalse(algorithm.getMessageStore().hasUUID(TEST_MESSAGE.getMetadata().getMessageID()));

        try {
            algorithm.addMessageToServer(TEST_MESSAGE);
            Assert.assertTrue(algorithm.getMessageStore().hasUUID(TEST_MESSAGE.getMetadata().getMessageID()));
            Assert.assertEquals(TEST_MESSAGE.getMessage().encodePrettily(),
                algorithm.getMessageStore().getMessage(TEST_MESSAGE.getMetadata().getMessageID()).encodePrettily());
        } catch (SameIDException e) {
            Assert.fail();
        }
    }

    @Test
    public void TestExampleAlgorithmDeleteMessageWillThrowErrorWhenNoIDPresentInMessages() {
        ExampleAlgorithm algorithm = createExampleAlgorithm();
        Assert.assertFalse(algorithm.getMessageStore().hasUUID(TEST_MESSAGE.getMetadata().getMessageID()));

        try {
            algorithm.deleteMessageFromServer(TEST_MESSAGE);
            Assert.fail();
        } catch (NoIDException e) {
            Assert.assertEquals(TEST_MESSAGE.getMetadata().getMessageID(), e.getId());
            Assert.assertEquals(String.format("Server does not contain message with id '%s'", TEST_MESSAGE.getMetadata().getMessageID()), e.toString());
        }
    }

    @Test
    public void TestExampleAlgorithmDeleteMessageDoesNotWhenIDPresentInMessages() {
        ExampleAlgorithm algorithm = createExampleAlgorithm();
        algorithm.getMessageStore().putMessage(TEST_MESSAGE.getMetadata().getMessageID(), TEST_MESSAGE.getMessage());
        Assert.assertTrue(algorithm.getMessageStore().hasUUID(TEST_MESSAGE.getMetadata().getMessageID()));

        try {
            algorithm.deleteMessageFromServer(TEST_MESSAGE);
            Assert.assertFalse(algorithm.getMessageStore().hasUUID(TEST_MESSAGE.getMetadata().getMessageID()));
        } catch (NoIDException e) {
            Assert.fail();
        }
    }

    private ExampleAlgorithm createExampleAlgorithm() {
        Controller controller = new Controller();
        MessageStore messageStore = new MessageStore();
        return new ExampleAlgorithm(controller, messageStore, vertx);
    }
}
