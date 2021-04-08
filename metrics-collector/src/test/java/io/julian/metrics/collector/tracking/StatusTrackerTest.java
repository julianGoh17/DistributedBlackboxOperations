package io.julian.metrics.collector.tracking;

import io.julian.metrics.collector.models.TrackedMessage;
import org.junit.Assert;
import org.junit.Test;

public class StatusTrackerTest {
    private final static String MESSAGE_ID = "message-id";
    private final static TrackedMessage SUCCESSFUL_MESSAGE = new TrackedMessage(200, MESSAGE_ID, 0.5f);
    private final static TrackedMessage FAILED_MESSAGE = new TrackedMessage(404, MESSAGE_ID, 10.5f);

    @Test
    public void TestInit() {
        StatusTracker tracker = new StatusTracker();
        Assert.assertEquals(0, tracker.getStatuses().size());
    }

    @Test
    public void TestAddNewStatus() {
        StatusTracker tracker = new StatusTracker();
        tracker.updateStatus(SUCCESSFUL_MESSAGE);
        Assert.assertEquals(1, tracker.getStatuses().size());
    }

    @Test
    public void TestAddToExistingStatus() {
        StatusTracker tracker = new StatusTracker();
        tracker.updateStatus(SUCCESSFUL_MESSAGE);
        Assert.assertEquals(1, tracker.getStatuses().size());
        tracker.updateStatus(FAILED_MESSAGE);
        Assert.assertEquals(1, tracker.getStatuses().size());
    }
}
