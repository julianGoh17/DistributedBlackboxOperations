package io.julian.metrics.collector.models;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class ErrorResponseTest {
    private final static int STATUS_CODE = 404;
    private final static Exception ERROR = new Exception("Not Found");
    private final static JsonObject JSON = new JsonObject()
        .put(ErrorResponse.STATUS_CODE_KEY, STATUS_CODE)
        .put(ErrorResponse.ERROR_KEY, ERROR.getMessage());

    @Test
    public void TestInitialization() {
        ErrorResponse response = new ErrorResponse(STATUS_CODE, ERROR);
        Assert.assertEquals(STATUS_CODE, response.getStatusCode());
        Assert.assertEquals(ERROR.getMessage(), response.getError().getMessage());
        Assert.assertEquals(JSON.encodePrettily(), response.toJson().encodePrettily());
    }

    @Test
    public void TestFromJson() {
        ErrorResponse response = JSON.mapTo(ErrorResponse.class);
        Assert.assertEquals(STATUS_CODE, response.getStatusCode());
        Assert.assertEquals(ERROR.getMessage(), response.getError().getMessage());
    }
}
