package io.julian.gossip.write;

import io.julian.gossip.models.UpdateResponse;
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

public class WriteReplyHandlerTest extends AbstractHandlerTest {
    private final static String MESSAGE_ID = "message";
    private final static boolean HAS_MESSAGE = true;
    private final static ClientMessage MESSAGE = new ClientMessage(HTTPRequest.POST, new JsonObject().put("random", "key"), MESSAGE_ID);

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
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);

        WriteReplyHandler replyHandler = createWriteReplyHandler();
        Async async = context.async();
        replyHandler.handleReply(MESSAGE, DEFAULT_SEVER_CONFIG)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(500, v1 -> {
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

        WriteReplyHandler replyHandler = createWriteReplyHandler();
        Async async = context.async();
        replyHandler.handleReply(MESSAGE, DEFAULT_SEVER_CONFIG)
            .onComplete(context.asyncAssertFailure(cause -> vertx.setTimer(500, v1 -> {
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                async.complete();
            })));
        async.awaitSuccess();
        collector.tearDownMetricsCollector(context);
    }

    private WriteReplyHandler createWriteReplyHandler() {
        return new WriteReplyHandler(createServerClient());
    }
}
