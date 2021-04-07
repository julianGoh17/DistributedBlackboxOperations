package io.julian.server.endpoints;

import io.julian.server.components.Configuration;
import io.julian.server.components.Server;
import io.julian.server.endpoints.gates.ProbabilisticFailureGate;
import io.julian.server.endpoints.gates.UnreachableGate;
import io.julian.server.models.response.ErrorResponse;
import io.julian.server.models.response.MessageIDResponse;
import io.julian.server.models.response.MessageResponse;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;


@RunWith(VertxUnitRunner.class)
public abstract class AbstractHandlerTest {
    public static final String CLIENT_URI = "/client";
    public static final String COORDINATOR_URI = "/coordinate";

    public static final Exception PROBABILISTIC_FAILURE_ERROR = new Exception(ProbabilisticFailureGate.FAILURE_MESSAGE);
    public static final Exception UNREACHABLE_ERROR = new Exception(UnreachableGate.FAILURE_MESSAGE);

    public Server server;
    public HttpServer api;
    public Vertx vertx;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    protected void setUpApiServer(final TestContext context) {
        server = new Server();
        Future<Boolean> hasDeployed = server.startServer(vertx, Configuration.DEFAULT_OPENAPI_SPEC_LOCATION);
        api = vertx.createHttpServer(new HttpServerOptions()
            .setPort(Configuration.DEFAULT_SERVER_PORT)
            .setHost(Configuration.DEFAULT_SERVER_HOST));

        Async async = context.async();
        hasDeployed.onComplete(context.asyncAssertSuccess(v ->
            api.requestHandler(server.getRouterFactory().getRouter()).listen(ar -> {
                context.assertTrue(ar.succeeded());
                async.complete();
            })));

        async.awaitSuccess();
    }

    protected JsonObject createPostMessage(final JsonObject message) {
        return new JsonObject()
            .put("message", message);
    }

    protected Future<String> sendSuccessfulGETMessage(final TestContext context, final WebClient client, final String messageId, final JsonObject message) {
        Promise<String> completed = Promise.promise();
        sendGETMessage(context, client, messageId)
            .compose(res -> {
                context.assertEquals(res.statusCode(), 200);
                context.assertEquals(res.bodyAsJsonObject().getJsonObject(MessageResponse.MESSAGE_KEY).encodePrettily(), message.encodePrettily());
                completed.complete(messageId);
                return Future.succeededFuture();
            });
        return completed.future();
    }

    protected Future<String> sendSuccessfulPOSTMessage(final TestContext context, final  WebClient client, final JsonObject message) {
        Promise<String> uuid = Promise.promise();
        sendPOSTMessage(context, client, createPostMessage(message))
            .compose(res -> {
                context.assertEquals(res.statusCode(), 200);
                context.assertNotNull(res.bodyAsJsonObject().getString(MessageIDResponse.MESSAGE_ID_KEY));
                uuid.complete(res.bodyAsJsonObject().getString(MessageIDResponse.MESSAGE_ID_KEY));
                return Future.succeededFuture();
            });
        return uuid.future();
    }

    protected void sendUnsuccessfulGETMessage(final TestContext context, final WebClient client,
                                               final String messageId, final Throwable error,
                                               final int expectedStatusCode) {
        sendGETMessage(context, client, messageId)
            .compose(res -> {
                context.assertEquals(res.statusCode(), expectedStatusCode);
                if (error != null) {
                    context.assertEquals(res.bodyAsJsonObject(), new ErrorResponse(expectedStatusCode, error).toJson());
                } else {
                    context.assertNull(res.bodyAsJsonObject());
                }
                return Future.succeededFuture();
            });
    }

    protected void sendUnsuccessfulPOSTMessage(final TestContext context, final WebClient client,
                                               final JsonObject message, final Throwable error,
                                               final int expectedStatusCode) {
        sendPOSTMessage(context, client, message)
            .compose(res -> {
                context.assertEquals(res.statusCode(), expectedStatusCode);
                if (error != null) {
                    context.assertEquals(res.bodyAsJsonObject(), new ErrorResponse(expectedStatusCode, error).toJson());
                } else {
                    context.assertNull(res.bodyAsJsonObject());
                }
                return Future.succeededFuture();
            });
    }

    protected Future<String> sendSuccessfulDELETEMessage(final TestContext context, final WebClient client, final String messageId, final boolean expectMessage) {
        Promise<String> completed = Promise.promise();
        sendDELETEMessage(context, client, messageId)
            .compose(res -> {
                context.assertEquals(200, res.statusCode());
                context.assertEquals(new MessageIDResponse(messageId).toJson().encodePrettily(), res.bodyAsJsonObject().encodePrettily());
                context.assertEquals(expectMessage, server.getMessages().hasUUID(messageId));
                completed.complete(messageId);
                return Future.succeededFuture();
            });
        return completed.future();
    }

    protected void sendUnsuccessfulDELETEMessage(final TestContext context, final WebClient client, final String messageId,
                                                 final int expectedStatusCode, final Exception exception) {
        sendDELETEMessage(context, client, messageId)
            .compose(res -> {
                context.assertEquals(expectedStatusCode, res.statusCode());
                context.assertEquals(new ErrorResponse(expectedStatusCode, exception).toJson().encodePrettily(),
                    res.bodyAsJsonObject().encodePrettily());
                return Future.succeededFuture();
            });
    }

    protected Future<HttpResponse<Buffer>> sendGETMessage(final TestContext context, final WebClient client, final String messageId) {
        Promise<HttpResponse<Buffer>> response = Promise.promise();
        client
            .get(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/?messageId=%s", CLIENT_URI, messageId))
            .send(context.asyncAssertSuccess(response::complete));
        return response.future();
    }

    protected Future<HttpResponse<Buffer>> sendPOSTMessage(final TestContext context, final WebClient client, final JsonObject requestBody) {
        Promise<HttpResponse<Buffer>> response = Promise.promise();
        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, CLIENT_URI)
            .sendJson(requestBody, context.asyncAssertSuccess(response::complete));
        return response.future();
    }

    protected Future<HttpResponse<Buffer>> sendDELETEMessage(final TestContext context, final WebClient client, final String messageId) {
        Promise<HttpResponse<Buffer>> response = Promise.promise();
        client
            .delete(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("%s/?messageId=%s", CLIENT_URI, messageId))
            .send(context.asyncAssertSuccess(response::complete));
        return response.future();
    }
}
