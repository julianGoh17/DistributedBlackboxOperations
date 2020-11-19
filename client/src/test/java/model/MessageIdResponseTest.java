package model;

import io.julian.client.model.responses.MessageIdResponse;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class MessageIdResponseTest {
    @Test
    public void TestMessageIdResponseMapsFromMessageID() {
        JsonObject message = new JsonObject().put("messageId", "this");
        MessageIdResponse response = message.mapTo(MessageIdResponse.class);

        Assert.assertNull(response.getError());
        Assert.assertEquals(0, response.getStatusCode());
        Assert.assertEquals(message.getString("messageId"), response.getMessageId());
    }

    @Test
    public void TestMessageIdResponseMapsErrorResponse() {
        JsonObject message = new JsonObject()
            .put("statusCode", 404)
            .put("error", "testError");
        MessageIdResponse response = message.mapTo(MessageIdResponse.class);

        Assert.assertEquals(message.getString("error"), response.getError());
        Assert.assertEquals(message.getInteger("statusCode").intValue(), response.getStatusCode());
        Assert.assertNull(response.getMessageId());
    }

    @Test
    public void TestMessageResponseIdGetterAndSetters() {
        int statusCode = 999;
        String error = "random-error";
        String id = "random-id";
        MessageIdResponse response = new JsonObject().mapTo(MessageIdResponse.class);

        response.setMessageId(id);
        response.setError(error);
        response.setStatusCode(statusCode);

        Assert.assertEquals(response.getError(), error);
        Assert.assertEquals(response.getMessageId(), id);
        Assert.assertEquals(response.getStatusCode(), statusCode);
    }
}
