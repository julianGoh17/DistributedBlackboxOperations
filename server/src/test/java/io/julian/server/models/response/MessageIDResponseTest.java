package io.julian.server.models.response;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class MessageIDResponseTest {
    private String uuid = "random-uid";

    @Test
    public void TestGetters() {
        MessageIDResponse response = new MessageIDResponse(uuid);
        Assert.assertEquals(uuid, response.getUuid());
    }

    @Test
    public void TestToJson() {
        MessageIDResponse response = new MessageIDResponse(uuid);
        JsonObject expectedJson = new JsonObject()
            .put("messageId", uuid);
        Assert.assertEquals(expectedJson, response.toJson());
    }


}
