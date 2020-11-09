package model;

import io.julian.client.model.GetMessageResponse;
import io.julian.server.models.MessageIDResponse;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class GetMessageResponseTest {
    @Test
    public void TestSendMessageResponseMapsFromMessage() {
        JsonObject message = new JsonObject().put("message", new JsonObject().put("this", "message"));
        GetMessageResponse response = message.mapTo(GetMessageResponse.class);

        Assert.assertNull(response.getError());
        Assert.assertEquals(0, response.getStatusCode());
        Assert.assertEquals(message.getJsonObject("message"), response.getMessage());
    }

    @Test
    public void TestSendMessageResponseMapsFromNullMessage() {
        JsonObject message = new JsonObject().put("message", new JsonObject());
        GetMessageResponse response = message.mapTo(GetMessageResponse.class);

        Assert.assertNull(response.getError());
        Assert.assertEquals(0, response.getStatusCode());
        Assert.assertEquals(message.getJsonObject("message"), response.getMessage());
    }

    @Test
    public void TestSendMessageResponseMapsErrorResponse() {
        JsonObject message = new JsonObject()
            .put("statusCode", 404)
            .put("error", "testError");
        GetMessageResponse response = message.mapTo(GetMessageResponse.class);

        Assert.assertEquals(message.getString("error"), response.getError());
        Assert.assertEquals(message.getInteger("statusCode").intValue(), response.getStatusCode());
        Assert.assertNull(response.getMessage());
    }

    @Test
    public void TestSendMessageResponseIdGetterAndSetters() {
        int statusCode = 999;
        String error = "random-error";
        Object message = new MessageIDResponse("random");
        GetMessageResponse response = new JsonObject().mapTo(GetMessageResponse.class);

        response.setMessage(message);
        response.setError(error);
        response.setStatusCode(statusCode);

        Assert.assertEquals(response.getError(), error);
        Assert.assertEquals(response.getMessage(), new JsonObject().put("uuid", "random"));
        Assert.assertEquals(response.getStatusCode(), statusCode);
    }
}
