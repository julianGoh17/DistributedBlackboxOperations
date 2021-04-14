package io.julian.gossip.write;

import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.models.UpdateResponse;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;
import tools.AbstractHandlerTest;
import tools.TestMetricsCollector;
import tools.TestServerComponents;

public class WriteHandlerTest extends AbstractHandlerTest {
    private final static String MESSAGE_ID = "random-id_123";
    private final static JsonObject JSON = new JsonObject().put("random", "key");
    private final static ClientMessage MESSAGE = new ClientMessage(HTTPRequest.POST, JSON, MESSAGE_ID);

    @Test
    public void TestShouldGoInactive() {
        GossipConfiguration configuration = new GossipConfiguration();
        configuration.setInactiveProbability(1);
        WriteHandler handler = createWriteHandler(configuration);
        Assert.assertTrue(handler.shouldGoInactive());

        configuration.setInactiveProbability(0);
        Assert.assertFalse(handler.shouldGoInactive());
    }

    @Test
    public void TestCreateCoordinationMessage() {
        WriteHandler handler = createWriteHandler();
        CoordinationMessage message = handler.getCoordinationMessage(JSON, MESSAGE_ID);
        Assert.assertEquals(HTTPRequest.POST, message.getMetadata().getRequest());
        Assert.assertEquals(WriteHandler.UPDATE_REQUEST_TYPE, message.getMetadata().getType());
        Assert.assertEquals(MESSAGE_ID, message.getMetadata().getMessageID());
        Assert.assertEquals(JSON.encodePrettily(), message.getMessage().encodePrettily());
    }

    @Test
    public void TestSuccessfullySendsClientMessageToServer(final TestContext context) {
        TestServerComponents components = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        TestMetricsCollector collector = setUpMetricsCollector(context);
        MessageStore messages = new MessageStore();
        State state = createState(messages);
        WriteHandler handler = createWriteHandler(state);

        Async async = context.async();
        handler.sendMessage(MESSAGE)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(500, v1 -> {
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(1, messages.getNumberOfMessages());
                async.complete();
            })));
        async.awaitSuccess();

        tearDownServer(context, components);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestSuccessfullyFailsToSendClientMessageToServer(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        MessageStore messages = new MessageStore();
        State state = createState(messages);
        WriteHandler handler = createWriteHandler(state);

        Async async = context.async();
        handler.sendMessage(MESSAGE)
            .onComplete(context.asyncAssertFailure(cause -> vertx.setTimer(500, v1 -> {
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(1, messages.getNumberOfMessages());
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                async.complete();
            })));
        async.awaitSuccess();

        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestSuccessfullyPropagatesMessagesToServer(final TestContext context) {
        TestServerComponents components = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        TestMetricsCollector collector = setUpMetricsCollector(context);
        MessageStore messages = new MessageStore();
        messages.putMessage(MESSAGE_ID, JSON);
        GossipConfiguration configuration = new GossipConfiguration();
        configuration.setInactiveProbability(0);
        State state = createState(messages);
        WriteHandler handler = createWriteHandler(state, configuration);

        Async async = context.async();
        handler.sendMessage(new UpdateResponse(MESSAGE_ID, true))
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(500, v1 -> {
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(1, messages.getNumberOfMessages());
                async.complete();
            })));
        async.awaitSuccess();

        collector.tearDownMetricsCollector(context);
        tearDownServer(context, components);
    }

    @Test
    public void TestDoesNotPropagatesMessageIfInactiveToServer(final TestContext context) {
        TestServerComponents components = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        TestMetricsCollector collector = setUpMetricsCollector(context);
        GossipConfiguration configuration = new GossipConfiguration();
        configuration.setInactiveProbability(1);
        State state = createState();
        WriteHandler handler = createWriteHandler(state, configuration);

        Async async = context.async();
        handler.sendMessage(new UpdateResponse(MESSAGE_ID, true))
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(500, v1 -> {
                collector.testHasExpectedStatusSize(0);
                async.complete();
            })));
        async.awaitSuccess();

        collector.tearDownMetricsCollector(context);
        tearDownServer(context, components);
    }

    @Test
    public void TestDoesNotPropagatesMessageIfDoesNotContainIDToServer(final TestContext context) {
        TestServerComponents components = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        TestMetricsCollector collector = setUpMetricsCollector(context);
        GossipConfiguration configuration = new GossipConfiguration();
        configuration.setInactiveProbability(0);
        State state = createState();
        WriteHandler handler = createWriteHandler(state, configuration);

        Async async = context.async();
        handler.sendMessage(new UpdateResponse(MESSAGE_ID, false))
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(500, v1 -> {
                collector.testHasExpectedStatusSize(0);
                async.complete();
            })));
        async.awaitSuccess();

        collector.tearDownMetricsCollector(context);
        tearDownServer(context, components);
    }

    private State createState() {
        return createState(new MessageStore());
    }

    private State createState(final MessageStore store) {
        return new State(store);
    }

    private WriteHandler createWriteHandler() {
        return createWriteHandler(createState(), new GossipConfiguration());
    }

    private WriteHandler createWriteHandler(final GossipConfiguration configuration) {
        return createWriteHandler(createState(), configuration);
    }

    private WriteHandler createWriteHandler(final State state) {
        return createWriteHandler(state, new GossipConfiguration());
    }

    private WriteHandler createWriteHandler(final State state, final GossipConfiguration configuration) {
        return new WriteHandler(createServerClient(), state, createTestRegistryManager(), configuration);
    }
}
