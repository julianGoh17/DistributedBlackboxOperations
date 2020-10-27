package endpoints;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
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
        sendSuccessfulPOSTMessage(context, client, message)
            .compose(messageId -> sendSuccessfulGETMessage(context, client, messageId, message));
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
