package io.julian.server.endpoints.coordination;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.AbstractHandlerTest;
import io.julian.server.models.ServerStatus;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMessageTest;
import io.julian.server.models.response.ErrorResponse;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class CoordinationMessageHandlerTest extends AbstractHandlerTest {
    public static final String COORDINATION_MESSAGE_ENDPOINT = "message";

    @Test
    public void TestSuccessfulCoordinationMessageCanLandInCoordinationQueue(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        CoordinationMessage message = CoordinationMessage.fromJson(CoordinationMessageTest.JSON);
        sendSuccessfulCoordinateMessage(context, client, message);
    }

    @Test
    public void TestCoordinationMessageFailsWhenMissingMetadataField(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        JsonObject missingMetadata = CoordinationMessageTest.JSON.copy();
        missingMetadata.remove(CoordinationMessage.METADATA_KEY);

        sendUnsuccessfulCoordinateMessage(context, client, missingMetadata, 400,
            new Exception(String.format(CoordinationMessage.DECODE_EXCEPTION_FORMAT_STRING, CoordinationMessage.METADATA_KEY))
        );
    }

    @Test
    public void TestFailsUnreachableGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.UNREACHABLE);

        sendUnsuccessfulCoordinateMessage(context, client, CoordinationMessageTest.JSON, 500, UNREACHABLE_ERROR);
    }

    @Test
    public void TestFailsProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(1);
        sendUnsuccessfulCoordinateMessage(context, client, CoordinationMessageTest.JSON, 500, PROBABILISTIC_FAILURE_ERROR);
    }

    @Test
    public void TestPassesProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(0);
        sendSuccessfulCoordinateMessage(context, client, CoordinationMessage.fromJson(CoordinationMessageTest.JSON));
    }

    private void sendSuccessfulCoordinateMessage(final TestContext context, final WebClient client, final CoordinationMessage message) {
        sendCoordinateMessage(context, client, message.toJson())
            .compose(res -> {
                context.assertEquals(200, res.statusCode());

                CoordinationMessage serverMessage = server.getController().getCoordinationMessage();
                context.assertEquals(serverMessage.getDefinition(), message.getDefinition());
                context.assertEquals(serverMessage.getMessage(), message.getMessage());

                context.assertEquals(serverMessage.getMetadata().getTimestamp().toLocalDateTime(), message.getMetadata().getTimestamp().toLocalDateTime());
                return Future.succeededFuture();
            });
    }

    private void sendUnsuccessfulCoordinateMessage(final TestContext context, final WebClient client, final JsonObject message,
                                                   final int statusCode, final Exception error) {
        sendCoordinateMessage(context, client, message)
            .compose(res -> {
                context.assertEquals(statusCode, res.statusCode());
                context.assertEquals(res.bodyAsJsonObject(), new ErrorResponse(statusCode, error).toJson());
                Assert.assertEquals(0, server.getController().getNumberOfCoordinationMessages());
                return Future.succeededFuture();
            });
    }

    private Future<HttpResponse<Buffer>> sendCoordinateMessage(final TestContext context, final WebClient client, final JsonObject message) {
        Promise<HttpResponse<Buffer>> response = Promise.promise();

        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/%s", COORDINATOR_URI, COORDINATION_MESSAGE_ENDPOINT))
            .sendJsonObject(message, context.asyncAssertSuccess(response::complete));

        return response.future();
    }
}
