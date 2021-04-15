package io.julian.gossip.delete;

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

import java.util.concurrent.ConcurrentLinkedQueue;

public class DeleteHandlerTest extends AbstractHandlerTest {
    private final static String DELETE_ID = "delete-id";

    @Test
    public void TestGetCoordinationMessage() {
        DeleteHandler deleteHandler = getDeleteHandler(createState());
        CoordinationMessage message = deleteHandler.getCoordinationMessage(DELETE_ID);
        Assert.assertEquals(HTTPRequest.DELETE, message.getMetadata().getRequest());
        Assert.assertEquals(DELETE_ID, message.getMetadata().getMessageID());
        Assert.assertEquals(DeleteHandler.TYPE, message.getMetadata().getType());
        Assert.assertEquals(DEFAULT_SEVER_CONFIG.toJson().encodePrettily(), message.getDefinition().encodePrettily());
        Assert.assertNull(message.getMessage());
    }

    @Test
    public void TestDealWithClientRequestSucceeds(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);

        ConcurrentLinkedQueue<CoordinationMessage> deadLetter = new ConcurrentLinkedQueue<>();
        MessageStore messageStore = new MessageStore();
        messageStore.putMessage(DELETE_ID, new JsonObject());
        DeleteHandler deleteHandler = getDeleteHandler(createState(messageStore, deadLetter));

        Async async = context.async();
        deleteHandler.dealWithClientMessage(new ClientMessage(HTTPRequest.DELETE, new JsonObject(), DELETE_ID))
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(1000, v1 -> {
                Assert.assertEquals(0, messageStore.getNumberOfMessages());
                Assert.assertEquals(0, deadLetter.size());
                collector.testHasExpectedStatusSize(1);
                async.complete();
            })));

        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestSendDeleteIfNotInactiveGoesInactive(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);

        ConcurrentLinkedQueue<CoordinationMessage> deadLetter = new ConcurrentLinkedQueue<>();
        MessageStore messageStore = new MessageStore();
        State state = createState(messageStore, deadLetter);
        GossipConfiguration configuration = new GossipConfiguration();
        configuration.setInactiveProbability(1f);
        DeleteHandler deleteHandler = getDeleteHandler(state, configuration);

        Async async = context.async();
        deleteHandler.sendDeleteIfNotInactive(new UpdateResponse(DELETE_ID, true))
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(1000, v1 -> {
                Assert.assertEquals(0, messageStore.getNumberOfMessages());
                Assert.assertEquals(0, deadLetter.size());
                Assert.assertEquals(1, state.getInactiveDeleteIds().size());
                collector.testHasExpectedStatusSize(0);
                async.complete();
            })));

        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestSendDeleteIfNotInactiveDoesNotGoInactive(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);

        ConcurrentLinkedQueue<CoordinationMessage> deadLetter = new ConcurrentLinkedQueue<>();
        MessageStore messageStore = new MessageStore();
        State state = createState(messageStore, deadLetter);
        GossipConfiguration configuration = new GossipConfiguration();
        configuration.setInactiveProbability(0f);
        DeleteHandler deleteHandler = getDeleteHandler(state, configuration);

        Async async = context.async();
        deleteHandler.sendDeleteIfNotInactive(new UpdateResponse(DELETE_ID, true))
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(1000, v1 -> {
                Assert.assertEquals(0, messageStore.getNumberOfMessages());
                Assert.assertEquals(0, deadLetter.size());
                Assert.assertEquals(0, state.getInactiveDeleteIds().size());
                collector.testHasExpectedStatusSize(1);
                async.complete();
            })));

        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestForwardDeleteRequestSucceeds(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);

        MessageStore messageStore = new MessageStore();
        messageStore.putMessage(DELETE_ID, new JsonObject());
        ConcurrentLinkedQueue<CoordinationMessage> deadLetter = new ConcurrentLinkedQueue<>();
        DeleteHandler deleteHandler = getDeleteHandler(createState(messageStore, deadLetter));

        Async async = context.async();
        deleteHandler.forwardDelete(DELETE_ID)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(1000, v1 -> {
                Assert.assertEquals(0, deadLetter.size());
                Assert.assertEquals(0, messageStore.getNumberOfMessages());
                collector.testHasExpectedStatusSize(1);
                async.complete();
            })));

        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestDeleteHandlerFails(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        ConcurrentLinkedQueue<CoordinationMessage> deadLetter = new ConcurrentLinkedQueue<>();
        MessageStore messageStore = new MessageStore();
        messageStore.putMessage(DELETE_ID, new JsonObject());
        DeleteHandler deleteHandler = getDeleteHandler(createState(messageStore, deadLetter));

        Async async = context.async();
        deleteHandler.forwardDelete(DELETE_ID)
            .onComplete(context.asyncAssertFailure(cause -> vertx.setTimer(1000, v1 -> {
                Assert.assertEquals(1, deadLetter.size());
                Assert.assertEquals(0, messageStore.getNumberOfMessages());
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                async.complete();
            })));

        async.awaitSuccess();
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestDeleteHandlerDoesNotErrorWhenDeletingNullKey(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        ConcurrentLinkedQueue<CoordinationMessage> deadLetter = new ConcurrentLinkedQueue<>();
        MessageStore messageStore = new MessageStore();
        DeleteHandler deleteHandler = getDeleteHandler(createState(messageStore, deadLetter));

        Async async = context.async();
        deleteHandler.forwardDelete(DELETE_ID)
            .onComplete(context.asyncAssertFailure(cause -> vertx.setTimer(1000, v1 -> {
                Assert.assertEquals(1, deadLetter.size());
                Assert.assertEquals(0, messageStore.getNumberOfMessages());
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                async.complete();
            })));

        async.awaitSuccess();
        collector.tearDownMetricsCollector(context);
    }

    private DeleteHandler getDeleteHandler(final State state) {
        return new DeleteHandler(createServerClient(), state, createTestRegistryManager(), new GossipConfiguration(), DEFAULT_SEVER_CONFIG);
    }

    private DeleteHandler getDeleteHandler(final State state, final GossipConfiguration configuration) {
        return new DeleteHandler(createServerClient(), state, createTestRegistryManager(), configuration, DEFAULT_SEVER_CONFIG);
    }
}
