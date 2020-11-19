package io.julian.server.models;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class LabelResponseTest {
    @Test
    public void TestMarkServerResponseMapsAppropriately() {
        String label = "test";
        JsonObject expected = new JsonObject().put("label", label);
        LabelResponse res = new LabelResponse(label);
        Assert.assertEquals(expected.encodePrettily(), res.toJson().encodePrettily());
        Assert.assertEquals(label, res.getLabel());
    }

    @Test
    public void TestLabelResponseFromJson() {
        JsonObject json = new JsonObject().put("label", "test");
        LabelResponse res = json.mapTo(LabelResponse.class);
        Assert.assertEquals("test", res.getLabel());
    }
}
