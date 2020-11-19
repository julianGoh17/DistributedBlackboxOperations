package io.julian.server.endpoints;

import io.julian.server.components.Configuration;
import io.julian.server.models.response.ErrorResponse;
import io.julian.server.models.response.LabelResponse;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

import static io.julian.server.components.Controller.DEFAULT_LABEL;

public class LabelHandlerTest extends AbstractHandlerTest {
    public static final String LABEL_ENDPOINT = "label";

    @Test
    public void TestSuccessfulPostMessageShouldUpdateServerLabel(final TestContext context) {
        String label = "test-table";
        setUpApiServer(context);

        WebClient client = WebClient.create(this.vertx);
        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/%s?label=%s", COORDINATOR_URI, LABEL_ENDPOINT, label))
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 202);
                context.assertEquals(new LabelResponse(label).toJson().encodePrettily(), res.bodyAsJsonObject().encodePrettily());
                context.assertEquals(label, server.getController().getLabel());
            }));
    }

    @Test
    public void TestPostMessageFailsWhenIncorrectQueryParam(final TestContext context) {
        setUpApiServer(context);

        WebClient client = WebClient.create(this.vertx);
        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/%s", COORDINATOR_URI, LABEL_ENDPOINT))
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(400, res.statusCode());
                context.assertEquals(new ErrorResponse(400,
                        new Exception("Error during validation of request. Parameter \"label\" inside query not found"))
                        .toJson()
                        .encodePrettily(),
                    res.bodyAsString());
                context.assertEquals(DEFAULT_LABEL, server.getController().getLabel());
            }));
    }
}
