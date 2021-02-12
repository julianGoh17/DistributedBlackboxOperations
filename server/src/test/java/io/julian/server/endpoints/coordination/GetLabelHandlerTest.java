package io.julian.server.endpoints.coordination;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.AbstractHandlerTest;
import io.julian.server.models.ServerStatus;
import io.julian.server.models.response.ErrorResponse;
import io.julian.server.models.response.LabelResponse;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.Assert;
import org.junit.Test;

import static io.julian.server.components.Controller.DEFAULT_LABEL;

public class GetLabelHandlerTest  extends AbstractHandlerTest {
    public static final String LABEL_ENDPOINT = "label";

    @Test
    public void TestCanSuccessfullyGetLabel(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(vertx);

        String label = "test-label";
        server.getController().setLabel(label);
        context.assertEquals(label, server.getController().getLabel());

        sendSuccessfulLabel(context, client, label);
    }

    @Test
    public void TestFailsUnreachableGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.UNREACHABLE);

        sendUnsuccessfulLabel(context, client, UNREACHABLE_ERROR);
    }

    @Test
    public void TestFailsProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(1);
        sendUnsuccessfulLabel(context, client, PROBABILISTIC_FAILURE_ERROR);
    }

    @Test
    public void TestPassesProbabilisticGateMessage(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(vertx);
        server.getController().setStatus(ServerStatus.PROBABILISTIC_FAILURE);
        server.getController().setFailureChance(0);

        String label = "test-label";
        server.getController().setLabel(label);
        context.assertEquals(label, server.getController().getLabel());

        sendSuccessfulLabel(context, client, label);
    }

    private void sendSuccessfulLabel(final TestContext context, final WebClient client, final String label) {
        Async async = context.async();
        sendLabel(context, client)
            .compose(res -> {
                context.assertEquals(res.statusCode(), 200);
                context.assertEquals(new LabelResponse(label).toJson().encodePrettily(), res.bodyAsJsonObject().encodePrettily());
                context.assertEquals(label, server.getController().getLabel());
                async.complete();
                return Future.succeededFuture();
            });
        async.awaitSuccess();
    }

    private void sendUnsuccessfulLabel(final TestContext context, final WebClient client, final Exception error) {
        Async async = context.async();
        sendLabel(context, client)
            .compose(res -> {
                context.assertEquals(500, res.statusCode());
                context.assertEquals(res.bodyAsJsonObject(), new ErrorResponse(500, error).toJson());
                Assert.assertEquals(0, server.getController().getNumberOfCoordinationMessages());
                context.assertEquals(DEFAULT_LABEL, server.getController().getLabel());

                async.complete();
                return Future.succeededFuture();
            });
        async.awaitSuccess();
    }

    private Future<HttpResponse<Buffer>> sendLabel(final TestContext context, final WebClient client) {
        Promise<HttpResponse<Buffer>> response = Promise.promise();

        String uri = String.format("%s/%s", COORDINATOR_URI, LABEL_ENDPOINT);

        client
            .get(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, uri)
            .send(context.asyncAssertSuccess(response::complete));

        return response.future();
    }
}
