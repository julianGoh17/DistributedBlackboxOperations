package io.julian.server.models;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class ErrorResponseTest {
    @Test
    public void TestGetters() {
        int errorCode = 404;
        Throwable exception = new Exception("Exception");

        ErrorResponse response = new ErrorResponse(errorCode, exception);
        Assert.assertEquals(errorCode, response.getStatusCode());
        Assert.assertEquals(exception, response.getException());
    }

    @Test
    public void TestToJson() {
        int errorCode = 404;
        Throwable exception = new Exception("Exception");

        JsonObject expected = new JsonObject()
            .put("statusCode", errorCode)
            .put("error", exception.getMessage());
        ErrorResponse response = new ErrorResponse(errorCode, exception);
        Assert.assertEquals(expected, response.toJson());
    }
}
