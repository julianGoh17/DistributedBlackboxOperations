package io.julian.server.endpoints.coordination;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.AbstractHandlerTest;
import io.julian.server.models.response.ErrorResponse;
import io.julian.server.models.ServerStatus;
import io.julian.server.models.response.SetStatusResponse;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

import static io.julian.server.components.Controller.DEFAULT_SERVER_STATUS;

public class SetStatusHandlerTest extends AbstractHandlerTest {
    private final static String SET_STATUS_ENDPOINT = "status";

    @Test
    public void TestSuccessfulPostMessageShouldUpdateServerState(final TestContext context) {
        String stringStatus = "unreachable";
        ServerStatus expectedStatus = ServerStatus.forValue(stringStatus);
        setUpApiServer(context);

        WebClient client = WebClient.create(this.vertx);
        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/%s?status=%s", COORDINATOR_URI, SET_STATUS_ENDPOINT, stringStatus))
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 202);
                context.assertEquals(new SetStatusResponse(expectedStatus).toJson().encodePrettily(), res.bodyAsJsonObject().encodePrettily());
                context.assertEquals(expectedStatus, server.getController().getStatus());
            }));
    }

    @Test
    public void TestPostMessageFailsWhenIncorrectQueryParam(final TestContext context) {
        setUpApiServer(context);

        WebClient client = WebClient.create(this.vertx);
        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/%s", COORDINATOR_URI, SET_STATUS_ENDPOINT))
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(400, res.statusCode());
                context.assertEquals(new ErrorResponse(400,
                    new Exception("Error during validation of request. Parameter \"status\" inside query not found"))
                        .toJson()
                        .encodePrettily(),
                    res.bodyAsString());
                context.assertEquals(DEFAULT_SERVER_STATUS, server.getController().getStatus());
            }));
    }
}
