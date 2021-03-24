package io.julian.server.components;

import io.julian.server.models.HTTPRequest;
import io.julian.server.models.ServerStatus;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class ControllerTest {
    @Test
    public void TestControllerInitializesProperly() {
        Controller controller = new Controller(new Configuration());
        Assert.assertEquals(ServerStatus.AVAILABLE, controller.getStatus());
        Assert.assertEquals("", controller.getLabel());
        checkQueueSizes(controller, 0, 0, 0);
    }

    @Test
    public void TestControllerCanSetStatus() {
        Controller controller = new Controller(new Configuration());
        controller.setStatus(ServerStatus.UNKNOWN);
        Assert.assertEquals(ServerStatus.UNKNOWN, controller.getStatus());
    }

    @Test
    public void TestControllerCanSetLabel() {
        String label = "random-label-123";
        Controller controller = new Controller(new Configuration());
        controller.setLabel(label);
        Assert.assertEquals(label, controller.getLabel());
    }

    @Test
    public void TestControllerGetServerConfiguration() {
        Controller controller = new Controller(new Configuration());
        ServerConfiguration configuration = controller.getServerConfiguration();
        Assert.assertEquals(Configuration.DEFAULT_SERVER_HOST, configuration.getHost());
        Assert.assertEquals(Configuration.DEFAULT_SERVER_PORT, configuration.getPort());
    }

    @Test
    public void TestAddToQueuesAndGetFromQueues() {
        Controller controller = new Controller(new Configuration());
        checkQueueSizes(controller, 0, 0, 0);

        controller.addToClientMessageQueue(new ClientMessage(HTTPRequest.POST, new JsonObject(), ""));
        checkQueueSizes(controller, 0, 1, 0);

        controller.addToCoordinationQueue(new CoordinationMessage(HTTPRequest.POST, new JsonObject()));
        checkQueueSizes(controller, 1, 1, 0);

        controller.addToDeadLetterQueue(new CoordinationMessage(HTTPRequest.POST, new JsonObject()));
        checkQueueSizes(controller, 1, 1, 1);

        controller.getDeadLetter();
        checkQueueSizes(controller, 1, 1, 0);

        controller.getCoordinationMessage();
        checkQueueSizes(controller, 0, 1, 0);

        controller.getClientMessage();
        checkQueueSizes(controller, 0, 0, 0);
    }

    private void checkQueueSizes(final Controller controller, final int coordinationMessages, final int clientMessages, final int deadLetters) {
        Assert.assertEquals(coordinationMessages, controller.getNumberOfCoordinationMessages());
        Assert.assertEquals(clientMessages, controller.getNumberOfClientMessages());
        Assert.assertEquals(deadLetters, controller.getNumberOfDeadLetters());
    }
}
