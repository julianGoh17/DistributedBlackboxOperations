package io.julian.server.endpoints.client;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.AbstractHandlerTest;
import io.julian.server.models.ServerStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

public class PutMessageHandlerTest extends AbstractHandlerTest {
    public static final JsonObject ORIGINAL_MESSAGE = new JsonObject().put("test", "message");
    public static final JsonObject NEW_MESSAGE = new JsonObject().put("new", "key");

    @Test
    public void TestPutMessageFailsWhenNoUUIDInServer(final TestContext context) {
        String invalidID = "does-not-exist";
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        sendUnsuccessfulPUTMessage(context, client, new JsonObject(), invalidID,
            new Exception(String.format("Could not find entry for uuid '%s'", invalidID)), 404);
    }

    @Test
    public void TestPutMessageSucceedsWhenUUIDInServer(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        sendSuccessfulPOSTMessage(context, client, ORIGINAL_MESSAGE)
            .compose(messageId -> sendSuccessfulGETMessage(context, client, messageId, ORIGINAL_MESSAGE))
            .compose(messageId -> sendSuccessfulPUTMessage(context, client, messageId, NEW_MESSAGE))
            .compose(messageId -> sendSuccessfulGETMessage(context, client, messageId, NEW_MESSAGE));
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

    @Test
    public void TestFailsUnreachableGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.UNREACHABLE);

        sendUnsuccessfulPUTMessage(context, client, createPostMessage(ORIGINAL_MESSAGE), "random-key",
            UNREACHABLE_ERROR, 500);
    }

    @Test
    public void TestFailsProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(1);
        sendUnsuccessfulPUTMessage(context, client, createPostMessage(ORIGINAL_MESSAGE), "random-key",
            PROBABILISTIC_FAILURE_ERROR, 500);
    }

    @Test
    public void TestPassesProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(0);

        sendSuccessfulPOSTMessage(context, client, ORIGINAL_MESSAGE)
            .compose(messageId -> sendSuccessfulGETMessage(context, client, messageId, ORIGINAL_MESSAGE))
            .compose(messageId -> sendSuccessfulPUTMessage(context, client, messageId, NEW_MESSAGE))
            .compose(messageId -> sendSuccessfulGETMessage(context, client, messageId, NEW_MESSAGE));
    }
}
