package io.julian.server.endpoints;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

import static io.julian.server.components.Server.DEFAULT_HOST;
import static io.julian.server.components.Server.DEFAULT_SERVER_PORT;

public class PostMessageHandlerTest extends AbstractHandlerTest {
    @Test
    public void TestSuccessfulMessageIsInDatabase(TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        JsonObject message = new JsonObject().put("test", "message");
        sendSuccessfulPOSTMessage(context, client, message).onComplete(id -> {
            context.assertEquals(1, server.getMessages().getNumberOfMessages());
            context.assertEquals(message, server.getMessages().getMessage(id.result()));
        });

    }

    @Test
    public void TestInvalidMessageRespondsWithError(TestContext context) {
        setUpApiServer(context);

        WebClient client = WebClient.create(this.vertx);
        sendUnsuccessfulPOSTMessage(context, client, new JsonObject(),
            new Exception("$.message: is missing but it is required"));
    }

    @Test
    public void TestPostMessageFailsWhenNoBodySent(TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        client
            .post(DEFAULT_SERVER_PORT, DEFAULT_HOST, CLIENT_URI)
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 400);
                context.assertNull(res.body());
            }));
    }
}
