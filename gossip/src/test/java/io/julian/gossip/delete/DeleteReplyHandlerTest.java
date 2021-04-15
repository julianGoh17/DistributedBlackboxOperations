package io.julian.gossip.delete;

import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.models.UpdateResponse;
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

public class DeleteReplyHandlerTest extends AbstractHandlerTest {
    private final static String DELETE_ID = "deleted-id";
    private final static boolean HAS_PROCESSED = true;

    @Test
    public void TestGetCoordinationMessage() {
        DeleteReplyHandler handler = getDeleteReplyHandler();
        CoordinationMessage message = handler.getCoordinationMessage(DELETE_ID, HAS_PROCESSED);

        Assert.assertEquals(HTTPRequest.DELETE, message.getMetadata().getRequest());
        Assert.assertEquals(DeleteReplyHandler.DELETE_REPLY_TYPE, message.getMetadata().getType());
        Assert.assertEquals(DELETE_ID + "-delete-reply", message.getMetadata().getMessageID());
        Assert.assertEquals(new UpdateResponse(DELETE_ID, HAS_PROCESSED).toJson().encodePrettily(), message.getDefinition().encodePrettily());
    }

    @Test
    public void TestHandleReplyCompletesSuccessfullyHandler(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context);
        TestMetricsCollector metricsCollector = setUpMetricsCollector(context);
        ConcurrentLinkedQueue<CoordinationMessage> deadLetter = new ConcurrentLinkedQueue<>();
        State state = createState(deadLetter);
        DeleteReplyHandler handler = getDeleteReplyHandler(state);
        state.addMessageIfNotInDatabase(DELETE_ID, new JsonObject());

        Async async = context.async();
        handler.handleReply(DELETE_ID, DEFAULT_SEVER_CONFIG)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(1000, v1 -> {
                metricsCollector.testHasExpectedStatusSize(1);
                Assert.assertEquals(0, state.getMessages().getNumberOfMessages());
                Assert.assertEquals(0, deadLetter.size());
                async.complete();
            })));
        async.awaitSuccess();
        tearDownServer(context, server);
        metricsCollector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestHandleReplyCompletesSuccessfullyWithDeleteIdHandler(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context);
        TestMetricsCollector metricsCollector = setUpMetricsCollector(context);
        ConcurrentLinkedQueue<CoordinationMessage> deadLetter = new ConcurrentLinkedQueue<>();
        State state = createState(deadLetter);
        DeleteReplyHandler handler = getDeleteReplyHandler(state);

        Async async = context.async();
        handler.handleReply(DELETE_ID, DEFAULT_SEVER_CONFIG)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(1000, v1 -> {
                metricsCollector.testHasExpectedStatusSize(1);
                Assert.assertEquals(0, state.getMessages().getNumberOfMessages());
                Assert.assertEquals(0, deadLetter.size());
                async.complete();
            })));
        async.awaitSuccess();
        tearDownServer(context, server);
        metricsCollector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestHandleReplyFails(final TestContext context) {
        TestMetricsCollector metricsCollector = setUpMetricsCollector(context);
        ConcurrentLinkedQueue<CoordinationMessage> deadLetter = new ConcurrentLinkedQueue<>();
        State state = createState(deadLetter);
        DeleteReplyHandler handler = getDeleteReplyHandler(state);

        Async async = context.async();
        handler.handleReply(DELETE_ID, DEFAULT_SEVER_CONFIG)
            .onComplete(context.asyncAssertFailure(cause -> vertx.setTimer(1000, v1 -> {
                metricsCollector.testHasExpectedStatusSize(1);
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                Assert.assertEquals(0, state.getMessages().getNumberOfMessages());
                Assert.assertEquals(1, deadLetter.size());
                async.complete();
            })));
        async.awaitSuccess();
        metricsCollector.tearDownMetricsCollector(context);
    }

    private DeleteReplyHandler getDeleteReplyHandler() {
        return getDeleteReplyHandler(createState());
    }

    private DeleteReplyHandler getDeleteReplyHandler(final State state) {
        return new DeleteReplyHandler(createServerClient(), state, createTestRegistryManager(), new GossipConfiguration());
    }
}
