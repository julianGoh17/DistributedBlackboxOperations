package io.julian.metrics.collector.models;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class SuccessResponseTest {
    private final static int STATUS_CODE = 404;
    private final static JsonObject JSON = new JsonObject()
        .put(SuccessResponse.STATUS_CODE_KEY, STATUS_CODE);

    @Test
    public void TestInit() {
        SuccessResponse response = new SuccessResponse(STATUS_CODE);
        Assert.assertEquals(STATUS_CODE, response.getStatusCode());
        Assert.assertEquals(JSON.encodePrettily(), response.toJson().encodePrettily());
    }

    @Test
    public void TestFromJson() {
        SuccessResponse response = JSON.mapTo(SuccessResponse.class);
        Assert.assertEquals(STATUS_CODE, response.getStatusCode());
    }
}
