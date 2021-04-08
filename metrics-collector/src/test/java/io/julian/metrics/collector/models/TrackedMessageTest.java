package io.julian.metrics.collector.models;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class TrackedMessageTest {
    private final static int STATUS_CODE = 200;
    private final static String MESSAGE_ID = "random-id";
    private final static float MESSAGE_SIZE = 10.5f;

    private final static JsonObject JSON = new JsonObject()
        .put(TrackedMessage.STATUS_CODE_KEY, STATUS_CODE)
        .put(TrackedMessage.MESSAGE_ID_KEY, MESSAGE_ID)
        .put(TrackedMessage.MESSAGE_SIZE_KEY, MESSAGE_SIZE);

    @Test
    public void TestInitialization() {
        TrackedMessage response = new TrackedMessage(STATUS_CODE, MESSAGE_ID, MESSAGE_SIZE);
        Assert.assertEquals(STATUS_CODE, response.getStatusCode());
        Assert.assertEquals(MESSAGE_ID, response.getMessageId());
        Assert.assertEquals(MESSAGE_SIZE, response.getMessageSize(), 0);
        Assert.assertEquals(JSON.encodePrettily(), response.toJson().encodePrettily());
    }

    @Test
    public void TestFromJson() {
        TrackedMessage response = JSON.mapTo(TrackedMessage.class);
        Assert.assertEquals(STATUS_CODE, response.getStatusCode());
        Assert.assertEquals(MESSAGE_ID, response.getMessageId());
        Assert.assertEquals(MESSAGE_SIZE, response.getMessageSize(), 0);
    }
}
