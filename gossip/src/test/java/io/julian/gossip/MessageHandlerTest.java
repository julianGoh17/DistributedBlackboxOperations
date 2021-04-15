package io.julian.gossip;

import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.models.UpdateResponse;
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

public class MessageHandlerTest extends AbstractHandlerTest {
    private final static String MESSAGE_ID = "id";
    @Test
    public void TestMessageHandlerRepliesAndSendsMessage(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);
        MessageStore messages = new MessageStore();
        MessageHandler handler = createTestMessageHandler(createState(messages));
        ClientMessage message = new ClientMessage(HTTPRequest.POST, new JsonObject(), MESSAGE_ID);
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
    public void TestMessageHandlerReplies(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);
        MessageStore messages = new MessageStore();
        messages.putMessage(MESSAGE_ID, new JsonObject());
        MessageHandler handler = createTestMessageHandler(createState(messages));
        CoordinationMessage message = new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.POST, MESSAGE_ID, WriteReplyHandler.WRITE_REPLY_TYPE),
            null,
            new UpdateResponse(MESSAGE_ID, false).toJson());

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
    public void TestMessageHandlerDealsWithClientMessage(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);
        MessageStore messages = new MessageStore();
        MessageHandler handler = createTestMessageHandler(createState(messages));
        ClientMessage message = new ClientMessage(HTTPRequest.POST, new JsonObject(), MESSAGE_ID);

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

    private MessageHandler createTestMessageHandler(final State state) {
        return new MessageHandler(createServerClient(), state, createTestRegistryManager(), new GossipConfiguration(), DEFAULT_SEVER_CONFIG);
    }
}
