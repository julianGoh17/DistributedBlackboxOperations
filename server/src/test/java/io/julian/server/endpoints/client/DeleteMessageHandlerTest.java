package io.julian.server.endpoints.client;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.control.AbstractServerHandlerTest;
import io.julian.server.models.ServerStatus;
import io.julian.server.models.response.ErrorResponse;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

public class DeleteMessageHandlerTest extends AbstractServerHandlerTest {
    private final static JsonObject TEST_MESSAGE = new JsonObject().put("test", "key");

    @Test
    public void TestSuccessfulDeleteMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        Async async = context.async();
        sendSuccessfulPOSTMessage(context, client, TEST_MESSAGE)
            .compose(id -> sendSuccessfulGETMessage(context, client, id, TEST_MESSAGE))
            .compose(id -> sendSuccessfulDELETEMessage(context, client, id, false))
            .compose(id -> sendUnsuccessfulGETMessage(context, client, id,
                new Exception(String.format("Could not find entry for uuid '%s'", id)), 404))
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context);
    }

    @Test
    public void TestSuccessfulSkipsDeleteMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        Async async = context.async();
        sendSuccessfulPOSTMessage(context, client, TEST_MESSAGE)
            .compose(id -> {
                server.getController().getConfiguration().setDoesProcessRequest(false);
                return sendSuccessfulGETMessage(context, client, id, TEST_MESSAGE);
            })
            .compose(id -> sendSuccessfulDELETEMessage(context, client, id, true))
            .compose(id -> {
                sendSuccessfulGETMessage(context, client, id, TEST_MESSAGE);
                return Future.succeededFuture();
            })
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context);
    }

    @Test
    public void TestUnsuccessfulDeleteMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        String invalidID = "invalid-id";
        Exception error = new Exception(String.format(DeleteMessageHandler.ERROR_RESPONSE, invalidID));
        Async async = context.async();
        sendUnsuccessfulDELETEMessage(context, client, invalidID, 404, error)
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context);
    }

    @Test
    public void TestFailsUnreachableGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.UNREACHABLE);

        Async async = context.async();
        sendUnsuccessfulDELETEMessage(context, client, "invalid-id", 500, UNREACHABLE_ERROR)
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context);
    }

    @Test
    public void TestFailsProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(1);
        Async async = context.async();
        sendUnsuccessfulDELETEMessage(context, client, "invalid-id", 500, PROBABILISTIC_FAILURE_ERROR)
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context);
    }

    @Test
    public void TestPassesProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(0);

        Async async = context.async();
        sendSuccessfulPOSTMessage(context, client, TEST_MESSAGE)
            .compose(id -> sendSuccessfulDELETEMessage(context, client, id, false))
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context);
    }

    @Test
    public void TestDELETEMessageFailsWhenNoPathParamPassedIn(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        Async async = context.async();
        client
            .delete(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, CLIENT_URI)
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 400);
                context.assertEquals(new ErrorResponse(400,
                        new Exception("Error during validation of request. Parameter \"messageId\" inside query not found")).toJson().encodePrettily(),
                    res.bodyAsJsonObject().encodePrettily());
                async.complete();
            }));
        async.awaitSuccess();
        tearDownServer(context);
    }
}
