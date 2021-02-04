package io.julian.server.endpoints.client;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.AbstractHandlerTest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

public class PutMessageHandlerTest extends AbstractHandlerTest {
    @Test
    public void TestPutMessageFailsWhenNoUUIDInServer(final TestContext context) {
        String invalidID = "does-not-exist";
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        client
            .put(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/%s", CLIENT_URI, invalidID))
            .sendJson(new JsonObject(), context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 404);
                context.assertEquals(res.bodyAsJsonObject().getInteger("statusCode"), 404);
                context.assertEquals(res.bodyAsJsonObject().getString("error"),
                    String.format("Could not find entry for uuid '%s'", invalidID));
            }));
    }

    @Test
    public void TestPutMessageSucceedsWhenUUIDInServer(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        JsonObject originalMessage = new JsonObject().put("test", "message");
        JsonObject newMessage = new JsonObject().put("new", "key");

        sendSuccessfulPOSTMessage(context, client, originalMessage)
            .compose(messageId -> sendSuccessfulGETMessage(context, client, messageId, originalMessage))
            .compose(messageId -> sendSuccessfulPUTMessage(context, client, messageId, newMessage))
            .compose(messageId -> sendSuccessfulGETMessage(context, client, messageId, newMessage));
    }

    @Test
    public void TestPutMessageFailsWhenNoPathParamPassedIn(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        client
            .get(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, CLIENT_URI)
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 405);
                context.assertNull(res.body());
            }));
    }

    @Test
    public void TestPutMessageFailsWhenNoBodySent(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        client
            .put(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/random", CLIENT_URI))
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 400);
                context.assertNull(res.body());
            }));
    }
}
