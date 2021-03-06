package io.julian.gossip.write;

import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.models.UpdateResponse;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
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

public class WriteReplyHandlerTest extends AbstractHandlerTest {
    private final static String MESSAGE_ID = "message";
    private final static boolean HAS_MESSAGE = true;

    @Test
    public void TestGetCoordinationMessage() {
        WriteReplyHandler handler = createWriteReplyHandler();
        CoordinationMessage message = handler.getCoordinationMessage(MESSAGE_ID, HAS_MESSAGE);
        Assert.assertEquals(HTTPRequest.POST, message.getMetadata().getRequest());
        Assert.assertEquals(WriteReplyHandler.WRITE_REPLY_TYPE, message.getMetadata().getType());
        Assert.assertEquals(MESSAGE_ID + "-reply", message.getMetadata().getMessageID());
        Assert.assertEquals(new UpdateResponse(MESSAGE_ID, HAS_MESSAGE).toJson().encodePrettily(), message.getDefinition().encodePrettily());
    }

    @Test
    public void TestHandleReplySuccessfullySendNoMessageResponse(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);

        MessageStore messages = new MessageStore();
        ConcurrentLinkedQueue<CoordinationMessage> deadLetters = new ConcurrentLinkedQueue<>();
        WriteReplyHandler replyHandler = createWriteReplyHandler(createState(messages, deadLetters));
        Async async = context.async();
        replyHandler.handleReply(MESSAGE_ID, new JsonObject(), DEFAULT_SEVER_CONFIG)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(500, v1 -> {
                Assert.assertEquals(0, deadLetters.size());
                Assert.assertEquals(1, messages.getNumberOfMessages());
                collector.testHasExpectedStatusSize(1);
                async.complete();
            })));
        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestHandleReplySuccessfullyRepliesAndDoesNotAddIfAlreadyAdded(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);

        MessageStore messages = new MessageStore();
        messages.putMessage(MESSAGE_ID, new JsonObject());
        ConcurrentLinkedQueue<CoordinationMessage> deadLetters = new ConcurrentLinkedQueue<>();
        WriteReplyHandler replyHandler = createWriteReplyHandler(createState(messages, deadLetters));
        Async async = context.async();
        replyHandler.handleReply(MESSAGE_ID, new JsonObject(), DEFAULT_SEVER_CONFIG)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(500, v1 -> {
                Assert.assertEquals(0, deadLetters.size());
                Assert.assertEquals(1, messages.getNumberOfMessages());
                collector.testHasExpectedStatusSize(1);
                async.complete();
            })));
        async.awaitSuccess();
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestHandleReplyFailsToSendNoMessageResponse(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);

        ConcurrentLinkedQueue<CoordinationMessage> deadLetters = new ConcurrentLinkedQueue<>();
        WriteReplyHandler replyHandler = createWriteReplyHandler(createState(deadLetters));
        Async async = context.async();
        replyHandler.handleReply(MESSAGE_ID, new JsonObject(), DEFAULT_SEVER_CONFIG)
            .onComplete(context.asyncAssertFailure(cause -> vertx.setTimer(500, v1 -> {
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(1, deadLetters.size());
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                async.complete();
            })));
        async.awaitSuccess();
        collector.tearDownMetricsCollector(context);
    }

    private WriteReplyHandler createWriteReplyHandler() {
        return createWriteReplyHandler(createState());
    }

    private WriteReplyHandler createWriteReplyHandler(final State state) {
        return new WriteReplyHandler(createServerClient(), state, createTestRegistryManager(), new GossipConfiguration());
    }
}
