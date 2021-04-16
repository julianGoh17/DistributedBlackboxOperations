package io.julian.metrics.collector.tracking;

import io.julian.metrics.collector.models.TrackedMessage;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MessageStatusTest {
    private final static TrackedMessage SUCCESSFUL_MESSAGE = new TrackedMessage(200, "random-id", 10.5f);
    private final static TrackedMessage FAILED_MESSAGE = new TrackedMessage(404, "random-id", 200f);

    private Vertx vertx;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @After
    public void after() {
        this.vertx.close();
    }

    @Test
    public void TestInit() {
        MessageStatus status = new MessageStatus();
        Assert.assertEquals(0, status.getFailedMessages());
        Assert.assertEquals(0, status.getTotalMessageSize(), 0);
        Assert.assertEquals(0, status.getSuccessfulMessages());
        Assert.assertEquals("0 minutes:0 seconds", status.getTimeDifference());
    }

    @Test
    public void TestAddSuccessfulMessage(final TestContext context) {
        MessageStatus status = new MessageStatus();

        Async async = context.async();
        vertx.setPeriodic(1000, id -> {
            status.addTrackedMessage(SUCCESSFUL_MESSAGE);
            Assert.assertEquals(0, status.getFailedMessages());
            Assert.assertEquals(SUCCESSFUL_MESSAGE.getMessageSize(), status.getTotalMessageSize(), 0);
            Assert.assertEquals(1, status.getSuccessfulMessages());
            Assert.assertEquals("0 minutes:1 seconds", status.getTimeDifference());
            async.complete();
        });
        async.awaitSuccess();
    }

    @Test
    public void TestAddFailedMessage(final TestContext context) {
        MessageStatus status = new MessageStatus();
        Async async = context.async();
        vertx.setPeriodic(1000, id -> {
            status.addTrackedMessage(FAILED_MESSAGE);
            Assert.assertEquals(1, status.getFailedMessages());
            Assert.assertEquals(FAILED_MESSAGE.getMessageSize(), status.getTotalMessageSize(), 0);
            Assert.assertEquals(0, status.getSuccessfulMessages());
            Assert.assertEquals("0 minutes:1 seconds", status.getTimeDifference());
            async.complete();
        });

        async.awaitSuccess();
    }
}
