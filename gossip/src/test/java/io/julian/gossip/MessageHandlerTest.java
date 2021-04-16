package io.julian.gossip;

import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.delete.DeleteHandler;
import io.julian.gossip.delete.DeleteReplyHandler;
import io.julian.gossip.models.MessageUpdate;
import io.julian.gossip.models.SynchronizeUpdate;
import io.julian.gossip.models.UpdateResponse;
import io.julian.gossip.synchronize.SynchronizeHandler;
import io.julian.gossip.write.WriteHandler;
import io.julian.gossip.write.WriteReplyHandler;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;
import tools.AbstractHandlerTest;
import tools.TestMetricsCollector;
import tools.TestServerComponents;

import java.util.Collections;
import java.util.HashSet;

public class MessageHandlerTest extends AbstractHandlerTest {
    private final static String STORED_ID = "stored";
    private final static String DELETED_ID = "deleted";
    private final static JsonObject JSON = new JsonObject().put("random", "key");

    @Test
    public void TestMessageHandlerRepliesAndSendsMessagePost(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);
        MessageStore messages = new MessageStore();
        MessageHandler handler = createTestMessageHandler(createState(messages));
        ClientMessage message = new ClientMessage(HTTPRequest.POST, new JsonObject(), STORED_ID);
        CoordinationMessage response = new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.POST, "id", WriteHandler.UPDATE_REQUEST_TYPE),
            message.toJson(),
            DEFAULT_SEVER_CONFIG.toJson());

        Async async = context.async();
        handler.handleCoordinationMessage(response)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(500, v1 -> {
                collector.testHasExpectedStatusSize(2);
                Assert.assertEquals(1, messages.getNumberOfMessages());
                async.complete();
            })));
        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestMessageHandlerRepliesPost(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);
        MessageStore messages = new MessageStore();
        messages.putMessage(STORED_ID, new JsonObject());
        MessageHandler handler = createTestMessageHandler(createState(messages));
        CoordinationMessage message = new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.POST, STORED_ID, WriteReplyHandler.WRITE_REPLY_TYPE),
            null,
            new UpdateResponse(STORED_ID, false).toJson());

        Async async = context.async();
        handler.handleCoordinationMessage(message)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(1000, v1 -> {
                collector.testHasExpectedStatusSize(1);
                async.complete();
            })));
        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestMessageHandlerDealsWithClientMessagePost(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);
        MessageStore messages = new MessageStore();
        MessageHandler handler = createTestMessageHandler(createState(messages));
        ClientMessage message = new ClientMessage(HTTPRequest.POST, new JsonObject(), STORED_ID);

        Async async = context.async();
        handler.handleClientMessage(message)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(500, v1 -> {
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(1, messages.getNumberOfMessages());
                async.complete();
            })));
        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestMessageHandlerRepliesAndSendsMessageDelete(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);
        MessageStore messages = new MessageStore();
        messages.putMessage(STORED_ID, new JsonObject());
        MessageHandler handler = createTestMessageHandler(createState(messages));
        ClientMessage message = new ClientMessage(HTTPRequest.DELETE, new JsonObject(), STORED_ID);
        CoordinationMessage response = new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.POST, "id", DeleteHandler.DELETE_UPDATE_TYPE),
            message.toJson(),
            DEFAULT_SEVER_CONFIG.toJson());

        Async async = context.async();
        handler.handleCoordinationMessage(response)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(500, v1 -> {
                collector.testHasExpectedStatusSize(2);
                Assert.assertEquals(1, messages.getNumberOfMessages());
                async.complete();
            })));
        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestMessageHandlerRepliesDelete(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);
        MessageStore messages = new MessageStore();
        MessageHandler handler = createTestMessageHandler(createState(messages));
        CoordinationMessage message = new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.DELETE, STORED_ID, DeleteReplyHandler.DELETE_REPLY_TYPE),
            null,
            new UpdateResponse(STORED_ID, false).toJson());

        Async async = context.async();
        handler.handleCoordinationMessage(message)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(1000, v1 -> {
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(0, messages.getNumberOfMessages());
                async.complete();
            })));
        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestMessageHandlerDealsWithClientMessageDelete(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);
        MessageStore messages = new MessageStore();
        messages.putMessage(STORED_ID, new JsonObject());
        MessageHandler handler = createTestMessageHandler(createState(messages));
        ClientMessage message = new ClientMessage(HTTPRequest.DELETE, new JsonObject(), STORED_ID);

        Async async = context.async();
        handler.handleClientMessage(message)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(500, v1 -> {
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(0, messages.getNumberOfMessages());
                async.complete();
            })));
        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestMessageHandlerBroadcastSynchronize(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);
        MessageHandler handler = createTestMessageHandler(createState());

        Async async = context.async();
        handler.broadcastState()
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(500, v1 -> {
                collector.testHasExpectedStatusSize(1);
                async.complete();
            })));
        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestMessageHandlerSynchronizesState(final TestContext context) {
        MessageStore messages = new MessageStore();
        State state = createState(messages);
        MessageHandler handler = createTestMessageHandler(state);
        CoordinationMessage message = new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.POST, "random", SynchronizeHandler.SYNCHRONIZE_TYPE),
            null,
            new SynchronizeUpdate(
                Collections.singletonList(new MessageUpdate(STORED_ID, JSON)),
                new HashSet<>(Collections.singletonList(DELETED_ID))).toJson()
        );

        Async async = context.async();
        handler.handleCoordinationMessage(message)
            .onComplete(context.asyncAssertSuccess(v -> {
                Assert.assertEquals(1, messages.getNumberOfMessages());
                Assert.assertTrue(state.isDeletedId(DELETED_ID));
                Assert.assertEquals(JSON.encodePrettily(), messages.getMessage(STORED_ID).encodePrettily());
                async.complete();
            }));
        async.awaitSuccess();
    }

    private MessageHandler createTestMessageHandler(final State state) {
        return new MessageHandler(createServerClient(), state, createTestRegistryManager(), new GossipConfiguration(), DEFAULT_SEVER_CONFIG, vertx);
    }
}
