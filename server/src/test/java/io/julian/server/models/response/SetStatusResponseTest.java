package io.julian.server.models.response;

import io.julian.server.models.ServerStatus;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class SetStatusResponseTest {
    @Test
    public void TestSetStatusResponseToJson() {
        ServerStatus status = ServerStatus.AVAILABLE;
        JsonObject expected = new JsonObject().put("status", "available");
        SetStatusResponse res = new SetStatusResponse(status);
        Assert.assertEquals(expected.encodePrettily(), res.toJson().encodePrettily());
        Assert.assertEquals(status, res.getStatus());
    }

    @Test
    public void TestSetStatusResponseFromJson() {
        JsonObject json = new JsonObject().put("status", "available");
        SetStatusResponse res = json.mapTo(SetStatusResponse.class);
        Assert.assertEquals(ServerStatus.AVAILABLE, res.getStatus());
    }
}
