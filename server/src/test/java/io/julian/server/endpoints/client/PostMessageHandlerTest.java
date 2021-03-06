package io.julian.server.endpoints.client;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.AbstractHandlerTest;
import io.julian.server.models.ServerStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

public class PostMessageHandlerTest extends AbstractHandlerTest {
    @Test
    public void TestSuccessfulMessageIsInDatabase(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        JsonObject message = new JsonObject().put("test", "message");
        Async async = context.async();
        sendSuccessfulPOSTMessage(context, client, message).onComplete(id -> {
            context.assertEquals(1, server.getMessages().getNumberOfMessages());
            context.assertEquals(message, server.getMessages().getMessage(id.result()));
            async.complete();
        });
        async.awaitSuccess();
    }

    @Test
    public void TestSuccessfulMessageSkipsAddingToDatabase(final TestContext context) {
        setUpApiServer(context);
        server.getController().getConfiguration().setDoesProcessRequest(false);
        WebClient client = WebClient.create(this.vertx);

        JsonObject message = new JsonObject().put("test", "message");
        Async async = context.async();
        sendSuccessfulPOSTMessage(context, client, message).onComplete(id -> {
            context.assertEquals(0, server.getMessages().getNumberOfMessages());
            async.awaitSuccess();
        });
        async.complete();
    }

    @Test
    public void TestFailsUnreachableGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.UNREACHABLE);

        JsonObject message = createPostMessage(new JsonObject().put("test", "message"));
        sendUnsuccessfulPOSTMessage(context, client, message, UNREACHABLE_ERROR, 500);
    }

    @Test
    public void TestFailsProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(1);
        JsonObject message = createPostMessage(new JsonObject().put("test", "message"));
        sendUnsuccessfulPOSTMessage(context, client, message, PROBABILISTIC_FAILURE_ERROR, 500);
    }

    @Test
    public void TestPassesProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(0);
        JsonObject message = new JsonObject().put("test", "message");
        sendSuccessfulPOSTMessage(context, client, message);
    }

    @Test
    public void TestInvalidMessageRespondsWithError(final TestContext context) {
        setUpApiServer(context);

        WebClient client = WebClient.create(this.vertx);
        sendUnsuccessfulPOSTMessage(context, client, new JsonObject(),
            new Exception("$.message: is missing but it is required"), 400);
    }

    @Test
    public void TestPostMessageFailsWhenNoBodySent(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, CLIENT_URI)
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 400);
                context.assertNull(res.body());
            }));
    }
}
