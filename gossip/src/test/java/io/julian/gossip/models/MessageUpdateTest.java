package io.julian.gossip.models;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class MessageUpdateTest {
    private final static String MESSAGE_ID = "random-4e2ed-id";
    private final static JsonObject MESSAGE = new JsonObject().put("random", new JsonObject().put("nested", "key"));
    private final static JsonObject JSON = new JsonObject()
        .put("messageId", MESSAGE_ID)
        .put("message", MESSAGE);
    @Test
    public void TestInit() {
        MessageUpdate update = new MessageUpdate(MESSAGE_ID, MESSAGE);
        Assert.assertEquals(MESSAGE_ID, update.getMessageId());
        Assert.assertEquals(MESSAGE.encodePrettily(), update.getMessage().encodePrettily());

        Assert.assertEquals(JSON.encodePrettily(), update.toJson().encodePrettily());
    }

    @Test
    public void TestFromJson() {
        MessageUpdate update = MessageUpdate.fromJson(JSON);
        Assert.assertEquals(MESSAGE_ID, update.getMessageId());
        Assert.assertEquals(MESSAGE.encodePrettily(), update.getMessage().encodePrettily());
    }
}
