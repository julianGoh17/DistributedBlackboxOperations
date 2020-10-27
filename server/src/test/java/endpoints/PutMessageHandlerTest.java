package endpoints;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import models.MessageIDResponse;
import org.junit.Test;

import static components.Server.DEFAULT_HOST;
import static components.Server.DEFAULT_SERVER_PORT;

public class PutMessageHandlerTest extends AbstractHandlerTest {
    @Test
    public void TestPutMessageFailsWhenNoUUIDInServer(TestContext context) {
        String invalidID = "does-not-exist";
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        client
            .put(DEFAULT_SERVER_PORT, DEFAULT_HOST, String.format("%s/%s", PutMessageHandler.URI, invalidID))
            .sendJson(new JsonObject(), context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 404);
                context.assertEquals(res.bodyAsJsonObject().getInteger("statusCode"), 404);
                context.assertEquals(res.bodyAsJsonObject().getString("error"), String.format("Could not find entry for uuid '%s'", invalidID));
            }));
    }

    @Test
    public void TestPutMessageSucceedsWhenUUIDInServer(TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        JsonObject originalMessage = new JsonObject().put("test", "message");
        JsonObject postedOriginalMessage = createPostMessage(originalMessage);

        JsonObject newMessage = new JsonObject().put("new", "key");
        JsonObject postedNewMessage = createPostMessage(newMessage);

        Promise<String> uuid = Promise.promise();
        client
            .post(DEFAULT_SERVER_PORT, DEFAULT_HOST, PostMessageHandler.URI)
            .sendJson(postedOriginalMessage, context.asyncAssertSuccess(res -> {
                context.assertNotNull(res);
                context.assertEquals(200, res.statusCode());
                context.assertNotNull(res.bodyAsJsonObject().getString(MessageIDResponse.MESSAGE_ID_KEY));
                uuid.complete(res.bodyAsJsonObject().getString(MessageIDResponse.MESSAGE_ID_KEY));
            }));

        uuid.future().compose(messageID -> {
            client
                .get(DEFAULT_SERVER_PORT, DEFAULT_HOST, String.format("%s/%s", GetMessageHandler.URI, messageID))
                .send(context.asyncAssertSuccess(res -> {
                    context.assertNotNull(res);
                    context.assertEquals(200, res.statusCode());
                    context.assertEquals(res.bodyAsJsonObject().encodePrettily(), originalMessage.encodePrettily());
                }));
            return Future.succeededFuture(messageID);
        }).compose(messageId -> {
            client
                .put(DEFAULT_SERVER_PORT, DEFAULT_HOST, String.format("%s/%s", PutMessageHandler.URI, messageId))
                .sendJson(postedNewMessage, context.asyncAssertSuccess(res -> {
                    context.assertEquals(res.statusCode(), 200);
                    context.assertEquals(res.bodyAsJsonObject().getString(MessageIDResponse.MESSAGE_ID_KEY), messageId);
                }));
            return Future.succeededFuture(messageId);
        }).compose(messageId -> {
            client
                .get(DEFAULT_SERVER_PORT, DEFAULT_HOST, String.format("%s/%s", GetMessageHandler.URI, messageId))
                .send(context.asyncAssertSuccess(res -> {
                    context.assertNotNull(res);
                    context.assertEquals(200, res.statusCode());
                    context.assertEquals(res.bodyAsJsonObject().encodePrettily(), newMessage.encodePrettily());
                }));
            return Future.succeededFuture();
        });
    }

    @Test
    public void TestPutMessageFailsWhenNoPathParamPassedIn(TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        client
            .get(DEFAULT_SERVER_PORT, DEFAULT_HOST, PutMessageHandler.URI)
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 405);
                context.assertNull(res.body());
            }));
    }

    @Test
    public void TestPutMessageFailsWhenNoBodySent(TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        client
            .put(DEFAULT_SERVER_PORT, DEFAULT_HOST, String.format("%s/random", PutMessageHandler.URI))
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 400);
                context.assertNull(res.body());
            }));
    }
}
