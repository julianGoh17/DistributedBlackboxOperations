package endpoints;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import models.MessageIDResponse;
import org.junit.Test;

import static components.Server.DEFAULT_HOST;
import static components.Server.DEFAULT_SERVER_PORT;

public class GetMessageHandlerTest extends AbstractHandlerTest {
    @Test
    public void TestGetMessageFailsWhenNoUUIDInServer(TestContext context) {
        String invalidID = "does-not-exist";
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        client
            .get(DEFAULT_SERVER_PORT, DEFAULT_HOST, String.format("%s/%s", CLIENT_URI, invalidID))
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 404);
                context.assertEquals(res.bodyAsJsonObject().getInteger("statusCode"), 404);
                context.assertEquals(res.bodyAsJsonObject().getString("error"), String.format("Could not find entry for uuid '%s'", invalidID));
            }));
    }

    @Test
    public void TestGetMessageSucceedsWhenUUIDInServer(TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        JsonObject message = new JsonObject().put("test", "message");
        JsonObject postedMessage = createPostMessage(message);
        Promise<String> uuid = Promise.promise();
        client
            .post(DEFAULT_SERVER_PORT, DEFAULT_HOST, CLIENT_URI)
            .sendJson(postedMessage, context.asyncAssertSuccess(res -> {
                context.assertNotNull(res);
                context.assertEquals(200, res.statusCode());
                context.assertNotNull(res.bodyAsJsonObject().getString(MessageIDResponse.MESSAGE_ID_KEY));
                uuid.complete(res.bodyAsJsonObject().getString(MessageIDResponse.MESSAGE_ID_KEY));
            }));

        uuid.future().onSuccess(messageId -> client
            .get(DEFAULT_SERVER_PORT, DEFAULT_HOST, String.format("%s/%s", CLIENT_URI, messageId))
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 200);
                context.assertEquals(res.bodyAsJsonObject().encodePrettily(), message.encodePrettily());
            })));
    }

    @Test
    public void TestGetMessageFailsWhenNoPathParamPassedIn(TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        client
            .get(DEFAULT_SERVER_PORT, DEFAULT_HOST, CLIENT_URI)
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 405);
                context.assertNull(res.body());
            }));
    }
}
