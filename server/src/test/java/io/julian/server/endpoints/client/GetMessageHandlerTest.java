package io.julian.server.endpoints.client;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.AbstractHandlerTest;
import io.julian.server.models.ServerStatus;
import io.julian.server.models.response.ErrorResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

public class GetMessageHandlerTest extends AbstractHandlerTest {
    @Test
    public void TestGetMessageFailsWhenNoUUIDInServer(final TestContext context) {
        String invalidID = "does-not-exist";
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        sendUnsuccessfulGETMessage(context, client, invalidID,
            new Exception(String.format("Could not find entry for uuid '%s'", invalidID)), 404);
    }

    @Test
    public void TestGetMessageSucceedsWhenUUIDInServer(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        JsonObject message = new JsonObject().put("test", "message");
        sendSuccessfulPOSTMessage(context, client, message)
            .compose(messageId -> sendSuccessfulGETMessage(context, client, messageId, message));
    }

    @Test
    public void TestGetMessageFailsWhenNoPathParamPassedIn(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        client
            .get(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, CLIENT_URI)
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 400);
                context.assertEquals(new ErrorResponse(400,
                        new Exception("Error during validation of request. Parameter \"messageId\" inside query not found")).toJson().encodePrettily(),
                    res.bodyAsJsonObject().encodePrettily());
            }));
    }

    @Test
    public void TestFailsUnreachableGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.UNREACHABLE);

        sendUnsuccessfulGETMessage(context, client, "random", UNREACHABLE_ERROR, 500);
    }

    @Test
    public void TestFailsProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(1);

        sendUnsuccessfulGETMessage(context, client, "random", PROBABILISTIC_FAILURE_ERROR, 500);
    }

    @Test
    public void TestPassesProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(0);

        JsonObject message = new JsonObject().put("test", "message");
        sendSuccessfulPOSTMessage(context, client, message)
            .compose(messageId -> sendSuccessfulGETMessage(context, client, messageId, message));
    }
}
