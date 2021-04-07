package io.julian.metrics.collector.tracking;

import io.julian.metrics.collector.models.TrackedMessage;
import org.junit.Assert;
import org.junit.Test;

public class MessageStatusTest {
    private final static TrackedMessage SUCCESSFUL_MESSAGE = new TrackedMessage(200, "random-id", 10.5f);
    private final static TrackedMessage FAILED_MESSAGE = new TrackedMessage(404, "random-id", 200f);

    @Test
    public void TestInit() {
        MessageStatus status = new MessageStatus();
        Assert.assertEquals(0, status.getFailedMessages());
        Assert.assertEquals(0, status.getTotalMessageSize(), 0);
        Assert.assertEquals(0, status.getSuccessfulMessages());
    }

    @Test
    public void TestAddSuccessfulMessage() {
        MessageStatus status = new MessageStatus();
        status.addTrackedMessage(SUCCESSFUL_MESSAGE);
        Assert.assertEquals(0, status.getFailedMessages());
        Assert.assertEquals(SUCCESSFUL_MESSAGE.getMessageSize(), status.getTotalMessageSize(), 0);
        Assert.assertEquals(1, status.getSuccessfulMessages());
    }

    @Test
    public void TestAddFailedMessage() {
        MessageStatus status = new MessageStatus();
        status.addTrackedMessage(FAILED_MESSAGE);
        Assert.assertEquals(1, status.getFailedMessages());
        Assert.assertEquals(FAILED_MESSAGE.getMessageSize(), status.getTotalMessageSize(), 0);
        Assert.assertEquals(0, status.getSuccessfulMessages());
    }
}
