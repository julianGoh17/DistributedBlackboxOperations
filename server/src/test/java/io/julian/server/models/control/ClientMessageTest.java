package io.julian.server.models.control;

import io.julian.server.models.HTTPRequest;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ClientMessageTest {
    private static final List<String> REQUESTS = Arrays.asList("GET", "POST", "PUT", "DELETE", "any");
    private static final JsonObject EXPECTED_MESSAGE = new JsonObject()
        .put("random", new JsonObject().put("nested", "key"));
    private static final String MESSAGE_ID = "original-id-1234";

    @Test
    public void TestCanMapFromValue() {
        for (String request : REQUESTS) {
            JsonObject targetMessage = new JsonObject()
                .put(ClientMessage.REQUEST_KEY, request)
                .put(ClientMessage.MESSAGE_KEY, EXPECTED_MESSAGE)
                .put(ClientMessage.MESSAGE_ID_KEY, MESSAGE_ID);

            ClientMessage message = ClientMessage.fromJson(targetMessage);
            Assert.assertEquals(HTTPRequest.forValue(request), message.getRequest());
            Assert.assertEquals(EXPECTED_MESSAGE.encodePrettily(), message.getMessage().encodePrettily());

            targetMessage.put(ClientMessage.REQUEST_KEY, HTTPRequest.forValue(request));
            Assert.assertEquals(targetMessage.encodePrettily(), message.toJson().encodePrettily());
        }
    }
}
