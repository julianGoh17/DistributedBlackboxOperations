package io.julian.server.endpoints.client;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.AbstractHandlerTest;
import io.julian.server.models.ServerStatus;
import io.julian.server.models.response.ErrorResponse;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

public class PutMessageHandlerTest extends AbstractHandlerTest {
    public static final JsonObject ORIGINAL_MESSAGE = new JsonObject().put("test", "message");
    public static final String ORIGINAL_ID = "original-id-1234";
    public static final String NEW_ID = "1234-new-id";
    @Test
    public void TestPutMessageFailsWhenNoUUIDInServer(final TestContext context) {
        String invalidID = "does-not-exist";
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        sendUnsuccessfulPUTMessage(context, client, invalidID, NEW_ID,
            new Exception(String.format("Could not find entry for uuid '%s'", invalidID)), 404);
    }

    @Test
    public void TestPutMessageSucceedsWhenUUIDInServer(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        sendSuccessfulPOSTMessage(context, client, ORIGINAL_MESSAGE)
            .compose(messageId -> sendSuccessfulPUTMessage(context, client, messageId, NEW_ID))
            .compose(messageID -> {
                sendUnsuccessfulGETMessage(context, client, messageID,
                    new Exception(String.format("Could not find entry for uuid '%s'", messageID)), 404);
                return Future.succeededFuture(messageID);
            })
            .compose(v -> sendSuccessfulGETMessage(context, client, NEW_ID, ORIGINAL_MESSAGE));
    }

    @Test
    public void TestPutMessageFailsWhenNoOriginalIdQueryParamPassedIn(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        client
            .put(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/?newId=random", CLIENT_URI))
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 400);
                context.assertEquals(new ErrorResponse(400,
                    new Exception("Error during validation of request. Parameter \"originalId\" inside query not found")).toJson().encodePrettily(),
                    res.bodyAsJsonObject().encodePrettily());
            }));
    }

    @Test
    public void TestPutMessageFailsNoNewIdQueryParamPassedIn(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        client
            .put(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/?originalId=random", CLIENT_URI))
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 400);
                context.assertEquals(new ErrorResponse(400,
                        new Exception("Error during validation of request. Parameter \"newId\" inside query not found")).toJson().encodePrettily(),
                    res.bodyAsJsonObject().encodePrettily());
            }));
    }

    @Test
    public void TestFailsUnreachableGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.UNREACHABLE);

        sendUnsuccessfulPUTMessage(context, client, ORIGINAL_ID, NEW_ID,
            UNREACHABLE_ERROR, 500);
    }

    @Test
    public void TestFailsProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(1);
        sendUnsuccessfulPUTMessage(context, client, ORIGINAL_ID, NEW_ID,
            PROBABILISTIC_FAILURE_ERROR, 500);
    }

    @Test
    public void TestPassesProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(0);

        sendSuccessfulPOSTMessage(context, client, ORIGINAL_MESSAGE)
            .compose(messageId -> sendSuccessfulPUTMessage(context, client, messageId, NEW_ID))
            .compose(messageID -> {
                sendUnsuccessfulGETMessage(context, client, messageID,
                    new Exception(String.format("Could not find entry for uuid '%s'", messageID)), 404);
                return Future.succeededFuture(messageID);
            })
            .compose(v -> sendSuccessfulGETMessage(context, client, NEW_ID, ORIGINAL_MESSAGE));
    }
}
