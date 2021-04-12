package io.julian.zookeeper.synchronize;

import io.julian.TestMetricsCollector;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.controller.State;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

public class LeaderSynchronizeHandlerTest extends AbstractServerBase {
    @Test
    public void TestCoordinationMessage() {
        LeaderSynchronizeHandler handler = createTestHandler();
        CoordinationMessage message = handler.getCoordinationMessage();
        Assert.assertEquals(HTTPRequest.UNKNOWN, message.getMetadata().getRequest());
        Assert.assertEquals(LeaderSynchronizeHandler.MESSAGE_ID, message.getMetadata().getMessageID());
        Assert.assertEquals(SynchronizeHandler.SYNCHRONIZE_TYPE, message.getMetadata().getType());

        Assert.assertEquals(SynchronizeHandler.SYNCHRONIZE_TYPE, message.getMetadata().getType());
        Assert.assertEquals(handler.getState().toJson().encodePrettily(), message.getDefinition().encodePrettily());
        Assert.assertNull(message.getMessage());
    }

    @Test
    public void TestBroadcastStateIsSuccessful(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        TestMetricsCollector collector = setUpMetricsCollector(context);
        LeaderSynchronizeHandler handler = createTestHandler();
        Async async = context.async();
        handler.broadcastState()
            .onComplete(context.asyncAssertSuccess(v1 -> vertx.setTimer(500, v2 -> {
                Assert.assertEquals(0, handler.getAcknowledgements());
                vertx.setTimer(500, v -> async.complete());
            })));
        async.awaitSuccess();
        collector.testHasExpectedStatusSize(1);
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestBroadcastStateFails(final TestContext context) {
        LeaderSynchronizeHandler handler = createTestHandler();
        Async async = context.async();
        handler.broadcastState()
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                Assert.assertEquals(0, handler.getAcknowledgements());
                Assert.assertEquals(1, handler.getDeadCoordinationMessages().size());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestIncrementAcknowledgement() {
        LeaderSynchronizeHandler handler = createTestHandler();
        Assert.assertEquals(0, handler.getAcknowledgements());
        handler.incrementAcknowledgement();
        Assert.assertEquals(1, handler.getAcknowledgements());
    }

    @Test
    public void TestHasEnoughAcknowledgements() {
        LeaderSynchronizeHandler handler = createTestHandler();
        RegistryManager manager = createTestRegistryManager();
        while (handler.getAcknowledgements() < manager.getOtherServers().size()) {
            Assert.assertFalse(handler.hasReceivedAcknowledgementsFromFollowers());
            handler.incrementAcknowledgement();
        }
        Assert.assertTrue(handler.hasReceivedAcknowledgementsFromFollowers());
    }

    private LeaderSynchronizeHandler createTestHandler() {
        return new LeaderSynchronizeHandler(new State(vertx, new MessageStore()), createTestRegistryManager(), createServerClient(), new ConcurrentLinkedQueue<>());
    }
}
