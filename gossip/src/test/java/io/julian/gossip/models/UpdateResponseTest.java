package io.julian.gossip.models;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class UpdateResponseTest {
    private final static String ID = "random-id";
    private final static boolean CONTAINS_ID = true;

    @Test
    public void TestInit() {
        UpdateResponse response = new UpdateResponse(ID, CONTAINS_ID);

        Assert.assertEquals(ID, response.getMessageId());
        Assert.assertEquals(CONTAINS_ID, response.getHasProcessedId());

        JsonObject expected = new JsonObject()
            .put("messageId", ID)
            .put("hasProcessedId", true);
        Assert.assertEquals(expected.encodePrettily(), response.toJson().encodePrettily());
    }
}
