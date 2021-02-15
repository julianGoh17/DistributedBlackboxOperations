package io.julian.api;

import io.julian.ExampleDistributedAlgorithm;
import io.julian.integration.AbstractServerBaseTest;
import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class ServerClientIntegrationTest extends AbstractServerBaseTest {
    private final static CoordinationMessage MESSAGE = new CoordinationMessage(
        new CoordinationMetadata("test-id", HTTPRequest.GET),
        new JsonObject(),
        new JsonObject());

    @Test
    public void TestClientCanSendMessage(final TestContext context) {
        setUpApiServer(context);
        ExampleDistributedAlgorithm algorithm = createExampleAlgorithm();

        Async async = context.async();
        algorithm.getRegistryManager().getOtherServers()
            .forEach(otherServerConfiguration ->
                algorithm.getClient().sendCoordinateMessageToServer(otherServerConfiguration, MESSAGE)
                    .onComplete(context.asyncAssertSuccess(v -> {
                        context.assertEquals(2, server.getController().getNumberOfCoordinationMessages());
                        async.complete();
                    })));
        async.await();
        tearDownServer(context);
    }

    @Test
    public void TestClientReturnsErrorSendMessage(final TestContext context) {
        ExampleDistributedAlgorithm algorithm = createExampleAlgorithm();

        Async async = context.async();
        algorithm.getRegistryManager().getOtherServers()
            .forEach(otherServerConfiguration ->
                algorithm.getClient().sendCoordinateMessageToServer(otherServerConfiguration, MESSAGE)
                    .onComplete(context.asyncAssertFailure(err -> {
                        context.assertEquals("Connection refused: localhost/127.0.0.1:8888", err.getMessage());
                        async.complete();
                    })));
        async.await();
    }

    @Test
    public void TestClientCanSETLabelServer(final TestContext context) {
        setUpApiServer(context);
        ExampleDistributedAlgorithm algorithm = createExampleAlgorithm();
        final String newLabel = "string";
        Async async = context.async();
        algorithm.getRegistryManager().getOtherServers()
            .forEach(otherServerConfiguration ->
                algorithm.getClient().sendLabelToServer(otherServerConfiguration, newLabel)
                    .onComplete(context.asyncAssertSuccess(v -> {
                        context.assertEquals(newLabel, server.getController().getLabel());
                        context.assertEquals(1, algorithm.getRegistryManager().getOtherServersWithLabel(newLabel).size());
                        async.complete();
                    })));
        async.awaitSuccess();
        tearDownServer(context);
    }

    @Test
    public void TestClientCanGETLabelServer(final TestContext context) {
        setUpApiServer(context);
        ExampleDistributedAlgorithm algorithm = createExampleAlgorithm();
        final String newLabel = "string";
        server.getController().setLabel(newLabel);
        context.assertEquals(newLabel, server.getController().getLabel());

        Async async = context.async();
        algorithm.getRegistryManager().getOtherServers()
            .forEach(otherServerConfiguration ->
                algorithm.getClient().getServerLabel(otherServerConfiguration)
                    .onComplete(context.asyncAssertSuccess(v -> {
                        context.assertEquals(1, algorithm.getRegistryManager().getOtherServersWithLabel(newLabel).size());
                        async.complete();
                    })));
        async.awaitSuccess();
        tearDownServer(context);
    }

    private ExampleDistributedAlgorithm createExampleAlgorithm() {
        Controller controller = new Controller();
        MessageStore messageStore = new MessageStore();
        ExampleDistributedAlgorithm algorithm = new ExampleDistributedAlgorithm(controller, messageStore, vertx);
        algorithm.getRegistryManager().registerServer(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT);
        return algorithm;
    }
}
