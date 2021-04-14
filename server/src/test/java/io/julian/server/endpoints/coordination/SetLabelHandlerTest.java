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
import org.junit.Test;

import static io.julian.server.components.Controller.DEFAULT_LABEL;

public class SetLabelHandlerTest extends AbstractHandlerTest {
    public static final String LABEL_ENDPOINT = "label";

    @Test
    public void TestSuccessfulPostMessageShouldUpdateServerLabel(final TestContext context) {
        String label = "test-table";
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        Async async = context.async();
        sendSuccessfulLabel(context, client, label)
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context);
    }

    @Test
    public void TestPostMessageFailsWhenIncorrectQueryParam(final TestContext context) {
        setUpApiServer(context);

        WebClient client = WebClient.create(this.vertx);
        Async async = context.async();
        sendUnsuccessfulLabel(context, client, null, new Exception("Error during validation of request. Parameter \"label\" inside query not found"), 400)
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
        sendUnsuccessfulLabel(context, client, "random-label", UNREACHABLE_ERROR, 500)
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
        sendUnsuccessfulLabel(context, client, "random-label", PROBABILISTIC_FAILURE_ERROR, 500)
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
        sendSuccessfulLabel(context, client, "random-label")
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context);
    }

    private Future<Void> sendSuccessfulLabel(final TestContext context, final WebClient client, final String label) {
        Promise<Void> response = Promise.promise();
        sendLabel(context, client, label)
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 202);
                context.assertEquals(new LabelResponse(label).toJson().encodePrettily(), res.bodyAsJsonObject().encodePrettily());
                context.assertEquals(label, server.getController().getLabel());
                response.complete();
            }));
        return response.future();
    }

    private Future<Void> sendUnsuccessfulLabel(final TestContext context, final WebClient client, final String label, final Exception error, final int statusCode) {
        Promise<Void> response = Promise.promise();
        sendLabel(context, client, label)
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(statusCode, res.statusCode());
                context.assertEquals(res.bodyAsJsonObject(), new ErrorResponse(statusCode, error).toJson());
                context.assertEquals(0, server.getController().getNumberOfCoordinationMessages());
                context.assertEquals(DEFAULT_LABEL, server.getController().getLabel());
                response.complete();
            }));
        return response.future();
    }

    private Future<HttpResponse<Buffer>> sendLabel(final TestContext context, final WebClient client, final String label) {
        Promise<HttpResponse<Buffer>> response = Promise.promise();

        String uri = label != null ?
            String.format("%s/%s?label=%s", COORDINATOR_URI, LABEL_ENDPOINT, label) : String.format("%s/%s", COORDINATOR_URI, LABEL_ENDPOINT);

        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, uri)
            .send(context.asyncAssertSuccess(response::complete));

        return response.future();
    }
}
