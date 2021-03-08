package io.julian.zookeeper.discovery;

import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.models.Zxid;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

public class LeaderDiscoveryHandlerTest extends AbstractServerBase {
    private final static Zxid LOWER_ID = new Zxid(1, 1);
    private final static Zxid MIDDLE_ID = new Zxid(1, 5);
    private final static Zxid HIGHER_ID = new Zxid(2, 3);

    @Test
    public void TestInit() {
        LeaderDiscoveryHandler handler = createHandler();
        Assert.assertEquals(0, handler.getEpoch());
        Assert.assertEquals(0, handler.getFollowerResponses());
        Assert.assertFalse(handler.hasEnoughResponses());
    }

    @Test
    public void TestProcessFollowerZXID() {
        LeaderDiscoveryHandler handler = createHandler();
        handler.processFollowerZXID(LOWER_ID);
        Assert.assertEquals(LOWER_ID.getEpoch(), handler.getEpoch());
        Assert.assertEquals(LOWER_ID.getCounter(), handler.getCounter());
        Assert.assertEquals(1, handler.getFollowerResponses());

        handler.processFollowerZXID(HIGHER_ID);
        Assert.assertEquals(HIGHER_ID.getEpoch(), handler.getEpoch());
        Assert.assertEquals(HIGHER_ID.getCounter(), handler.getCounter());
        Assert.assertEquals(2, handler.getFollowerResponses());

        handler.processFollowerZXID(MIDDLE_ID);
        Assert.assertEquals(HIGHER_ID.getEpoch(), handler.getEpoch());
        Assert.assertEquals(HIGHER_ID.getCounter(), handler.getCounter());
        Assert.assertEquals(3, handler.getFollowerResponses());
    }

    @Test
    public void TestHasEnoughResponses() {
        LeaderDiscoveryHandler handler = createHandler();
        Assert.assertFalse(handler.hasEnoughResponses());

        handler.processFollowerZXID(LOWER_ID);
        Assert.assertTrue(handler.hasEnoughResponses());
    }

    @Test
    public void TestUpdateStateEpochAndCounter() {
        LeaderDiscoveryHandler handler = createHandler();
        Assert.assertEquals(0, handler.getState().getCounter());
        Assert.assertEquals(0, handler.getState().getLeaderEpoch());
        handler.processFollowerZXID(LOWER_ID);
        handler.updateStateEpochAndCounter();

        Assert.assertEquals(LOWER_ID.getCounter(), handler.getState().getCounter());
        Assert.assertEquals(LOWER_ID.getEpoch(), handler.getState().getLeaderEpoch());
    }

    @Test
    public void TestReset() {
        LeaderDiscoveryHandler handler = createHandler();
        handler.processFollowerZXID(LOWER_ID);
        handler.processFollowerZXID(HIGHER_ID);
        Assert.assertEquals(HIGHER_ID.getEpoch(), handler.getEpoch());
        Assert.assertEquals(HIGHER_ID.getCounter(), handler.getCounter());
        Assert.assertEquals(2, handler.getFollowerResponses());

        handler.reset();
        Assert.assertEquals(handler.getState().getLeaderEpoch(), handler.getEpoch());
        Assert.assertEquals(handler.getState().getCounter(), handler.getCounter());
        Assert.assertEquals(0, handler.getFollowerResponses());
    }

    @Test
    public void TestBroadcastGatherZXIDSucceeds(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        LeaderDiscoveryHandler handler = createHandler();
        Async async = context.async();
        handler.broadcastGatherZXID()
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestBroadcastGatherZXIDFails(final TestContext context) {
        LeaderDiscoveryHandler handler = createHandler();
        Async async = context.async();
        handler.broadcastGatherZXID()
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestBroadcastStateUpdateSucceeds(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        LeaderDiscoveryHandler handler = createHandler();
        Async async = context.async();
        handler.broadcastLeaderState()
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestBroadcastStateUpdateFails(final TestContext context) {
        LeaderDiscoveryHandler handler = createHandler();
        Async async = context.async();
        handler.broadcastLeaderState()
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestCreateBroadcastMessage() {
        LeaderDiscoveryHandler handler = createHandler();
        String type = "random-type";
        CoordinationMessage message = handler.createBroadcastMessage(type);
        Assert.assertEquals(type, message.getMetadata().getType());
        Assert.assertEquals(HTTPRequest.UNKNOWN, message.getMetadata().getRequest());
        Assert.assertNull(message.getMessage());
        Assert.assertNull(message.getDefinition());
        Assert.assertNotNull(message.toJson().encodePrettily());
    }

    @Test
    public void TestCreateStateUpdate() {
        LeaderDiscoveryHandler handler = createHandler();
        CoordinationMessage message = handler.createStateUpdate();
        Assert.assertEquals(LeaderDiscoveryHandler.LEADER_STATE_UPDATE_TYPE, message.getMetadata().getType());
        Assert.assertEquals(HTTPRequest.UNKNOWN, message.getMetadata().getRequest());
        Assert.assertEquals(new Zxid(0, 0).toJson().encodePrettily(), message.getDefinition().encodePrettily());
        Assert.assertNull(message.getMessage());
        Assert.assertNotNull(message.toJson().encodePrettily());
    }

    private LeaderDiscoveryHandler createHandler() {
        return new LeaderDiscoveryHandler(new State(vertx, new MessageStore()), createTestRegistryManager(), createServerClient());
    }
}
