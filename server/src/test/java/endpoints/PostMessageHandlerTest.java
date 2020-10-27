package endpoints;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import models.ErrorResponse;
import models.MessageIDResponse;
import org.junit.Test;

import static components.Server.DEFAULT_HOST;
import static components.Server.DEFAULT_SERVER_PORT;

public class PostMessageHandlerTest extends AbstractHandlerTest {
    @Test
    public void TestSuccessfulMessageIsInDatabase(TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        JsonObject message = createPostMessage(new JsonObject().put("test", "message"));
        Promise<String> uuid = Promise.promise();
        client
            .post(DEFAULT_SERVER_PORT, DEFAULT_HOST, PostMessageHandler.URI)
            .sendJson(message, context.asyncAssertSuccess(res -> {
                context.assertNotNull(res);
                context.assertEquals(200, res.statusCode());
                context.assertNotNull(res.bodyAsJsonObject().getString(MessageIDResponse.MESSAGE_ID_KEY));
                uuid.complete(res.bodyAsJsonObject().getString(MessageIDResponse.MESSAGE_ID_KEY));
            }));

        uuid.future().onComplete(id -> {
            context.assertEquals(1, server.getMessages().getNumberOfMessages());
            context.assertEquals(message.getJsonObject("message"), server.getMessages().getMessage(id.result()));
        });

    }

    @Test
    public void TestInvalidMessageRespondsWithError(TestContext context) {
        setUpApiServer(context);

        ErrorResponse expectedError = new ErrorResponse(400, new Exception("$.message: is missing but it is required"));
        WebClient client = WebClient.create(this.vertx);
        client
            .post(DEFAULT_SERVER_PORT, DEFAULT_HOST, PostMessageHandler.URI)
            .sendJson(new JsonObject(), context.asyncAssertSuccess(res -> {
                context.assertNotNull(res);
                context.assertEquals(400, res.statusCode());
                context.assertEquals(expectedError.toJson().encodePrettily(), res.bodyAsString());
            }));
    }
}
