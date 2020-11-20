package io.julian.server.endpoints;

import io.julian.server.components.Configuration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMessageTest;
import io.julian.server.models.response.ErrorResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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
        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/%s", COORDINATOR_URI, COORDINATION_MESSAGE_ENDPOINT))
            .sendJsonObject(message.toJson(), context.asyncAssertSuccess(res -> {
                context.assertEquals(200, res.statusCode());

                CoordinationMessage serverMessage = server.getController().getCoordinationMessage();
                context.assertEquals(serverMessage.getDefinition(), message.getDefinition());
                context.assertEquals(serverMessage.getMessage(), message.getMessage());

                context.assertEquals(serverMessage.getMetadata().getFromServerId(), message.getMetadata().getFromServerId());
                context.assertEquals(serverMessage.getMetadata().getTimestamp().toLocalDateTime(), message.getMetadata().getTimestamp().toLocalDateTime());
            }));
    }

    @Test
    public void TestCoordinationMessageFailsWhenMissingMetadataField(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        JsonObject missingMetadata = CoordinationMessageTest.JSON.copy();
        missingMetadata.remove(CoordinationMessage.METADATA_KEY);
        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/%s", COORDINATOR_URI, COORDINATION_MESSAGE_ENDPOINT))
            .sendJsonObject(missingMetadata, context.asyncAssertSuccess(res -> {
                context.assertEquals(500, res.statusCode());
                Assert.assertEquals(new ErrorResponse(500, new Exception(String.format(CoordinationMessage.DECODE_EXCEPTION_FORMAT_STRING, CoordinationMessage.METADATA_KEY))).toJson().encodePrettily(), res.bodyAsString());
                Assert.assertEquals(0, server.getController().getNumberOfCoordinationMessages());
            }));
    }

    @Test
    public void TestCoordinationMessageFailsWhenMissingMessageField(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        JsonObject missingMessage = CoordinationMessageTest.JSON.copy();
        missingMessage.remove(CoordinationMessage.MESSAGE_KEY);
        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/%s", COORDINATOR_URI, COORDINATION_MESSAGE_ENDPOINT))
            .sendJsonObject(missingMessage, context.asyncAssertSuccess(res -> {
                context.assertEquals(500, res.statusCode());
                Assert.assertEquals(new ErrorResponse(500, new Exception(String.format(CoordinationMessage.DECODE_EXCEPTION_FORMAT_STRING, CoordinationMessage.MESSAGE_KEY))).toJson().encodePrettily(), res.bodyAsString());
                Assert.assertEquals(0, server.getController().getNumberOfCoordinationMessages());
            }));
    }

    @Test
    public void TestCoordinationMessageFailsWhenMissingDefinitionField(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        JsonObject missingDefinition = CoordinationMessageTest.JSON.copy();
        missingDefinition.remove(CoordinationMessage.DEFINITION_KEY);
        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/%s", COORDINATOR_URI, COORDINATION_MESSAGE_ENDPOINT))
            .sendJsonObject(missingDefinition, context.asyncAssertSuccess(res -> {
                context.assertEquals(500, res.statusCode());
                Assert.assertEquals(new ErrorResponse(500, new Exception(String.format(CoordinationMessage.DECODE_EXCEPTION_FORMAT_STRING, CoordinationMessage.DEFINITION_KEY))).toJson().encodePrettily(), res.bodyAsString());
                Assert.assertEquals(0, server.getController().getNumberOfCoordinationMessages());
            }));
    }
}
