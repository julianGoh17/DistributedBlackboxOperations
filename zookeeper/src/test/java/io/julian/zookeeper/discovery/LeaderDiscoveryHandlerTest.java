package io.julian.zookeeper.discovery;

import io.julian.server.components.MessageStore;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.models.Zxid;
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
    public void TestProcessFollowerEpoch() {
        LeaderDiscoveryHandler handler = createHandler();
        handler.processFollowerEpoch(LOWER_ID);
        Assert.assertEquals(LOWER_ID.getEpoch(), handler.getEpoch());
        Assert.assertEquals(LOWER_ID.getCounter(), handler.getCounter());
        Assert.assertEquals(1, handler.getFollowerResponses());

        handler.processFollowerEpoch(HIGHER_ID);
        Assert.assertEquals(HIGHER_ID.getEpoch(), handler.getEpoch());
        Assert.assertEquals(HIGHER_ID.getCounter(), handler.getCounter());
        Assert.assertEquals(2, handler.getFollowerResponses());

        handler.processFollowerEpoch(MIDDLE_ID);
        Assert.assertEquals(HIGHER_ID.getEpoch(), handler.getEpoch());
        Assert.assertEquals(HIGHER_ID.getCounter(), handler.getCounter());
        Assert.assertEquals(3, handler.getFollowerResponses());
    }

    @Test
    public void TestHasEnoughResponses() {
        LeaderDiscoveryHandler handler = createHandler();
        Assert.assertFalse(handler.hasEnoughResponses());

        handler.processFollowerEpoch(LOWER_ID);
        Assert.assertTrue(handler.hasEnoughResponses());
    }

    @Test
    public void TestUpdateStateEpochAndCounter() {
        LeaderDiscoveryHandler handler = createHandler();
        Assert.assertEquals(0, handler.getState().getCounter());
        Assert.assertEquals(0, handler.getState().getLeaderEpoch());
        handler.processFollowerEpoch(LOWER_ID);
        handler.updateStateEpochAndCounter();

        Assert.assertEquals(LOWER_ID.getCounter(), handler.getState().getCounter());
        Assert.assertEquals(LOWER_ID.getEpoch(), handler.getState().getLeaderEpoch());
    }

    @Test
    public void TestReset() {
        LeaderDiscoveryHandler handler = createHandler();
        handler.processFollowerEpoch(LOWER_ID);
        handler.processFollowerEpoch(HIGHER_ID);
        Assert.assertEquals(HIGHER_ID.getEpoch(), handler.getEpoch());
        Assert.assertEquals(HIGHER_ID.getCounter(), handler.getCounter());
        Assert.assertEquals(2, handler.getFollowerResponses());

        handler.reset();
        Assert.assertEquals(handler.getState().getLeaderEpoch(), handler.getEpoch());
        Assert.assertEquals(handler.getState().getCounter(), handler.getCounter());
        Assert.assertEquals(0, handler.getFollowerResponses());
    }

    private LeaderDiscoveryHandler createHandler() {
        return new LeaderDiscoveryHandler(new State(vertx, new MessageStore()), createServerClient(), createTestRegistryManager());
    }
}
