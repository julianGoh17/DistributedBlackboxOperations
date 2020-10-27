package endpoints;

import components.Server;
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
import models.ErrorResponse;
import models.MessageIDResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import static components.Server.DEFAULT_HOST;
import static components.Server.DEFAULT_SERVER_PORT;
import static components.Server.OPENAPI_SPEC_LOCATION;

@RunWith(VertxUnitRunner.class)
public abstract class AbstractHandlerTest {
    public static final String CLIENT_URI = "/client";

    Server server;
    HttpServer api;
    Vertx vertx;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    protected void setUpApiServer(TestContext context) {
        server = new Server();
        Promise<Boolean> hasDeployed = server.startServer(vertx, OPENAPI_SPEC_LOCATION);
        api = vertx.createHttpServer(new HttpServerOptions()
            .setPort(DEFAULT_SERVER_PORT)
            .setHost(DEFAULT_HOST));

        Async async = context.async();
        hasDeployed.future().onComplete(context.asyncAssertSuccess(v -> {
            api.requestHandler(server.getRouterFactory().getRouter()).listen(ar -> {
                context.assertTrue(ar.succeeded());
                async.complete();
            });
        }));

        async.awaitSuccess();
    }

    protected JsonObject createPostMessage(JsonObject message) {
        return new JsonObject()
            .put("message", message);
    }

    protected Future<String> sendSuccessfulGETMessage(TestContext context, WebClient client, String messageId, JsonObject message) {
        Promise<String> completed = Promise.promise();
        client
            .get(DEFAULT_SERVER_PORT, DEFAULT_HOST, String.format("%s/%s", CLIENT_URI, messageId))
            .send(context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 200);
                context.assertEquals(res.bodyAsJsonObject().encodePrettily(), message.encodePrettily());
                completed.complete(messageId);
            }));
        return completed.future();
    }

    protected Future<String> sendSuccessfulPOSTMessage(TestContext context, WebClient client, JsonObject message) {
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

    protected Future<String> sendSuccessfulPUTMessage(TestContext context, WebClient client, String messageId, JsonObject message) {
        Promise<String> completed = Promise.promise();
        client
            .put(DEFAULT_SERVER_PORT, DEFAULT_HOST, String.format("%s/%s", CLIENT_URI, messageId))
            .sendJson(createPostMessage(message), context.asyncAssertSuccess(res -> {
                context.assertEquals(res.statusCode(), 200);
                context.assertEquals(res.bodyAsJsonObject().getString(MessageIDResponse.MESSAGE_ID_KEY), messageId);
                completed.complete(messageId);
            }));
        return completed.future();
    }

    protected void sendUnsuccessfulPOSTMessage(TestContext context, WebClient client, JsonObject message, int statusCode, Throwable error) {
        sendPOSTMessage(context, client, message)
            .compose(res -> {
                context.assertEquals(res.statusCode(), statusCode);
                if (error != null) {
                    context.assertEquals(res.bodyAsJsonObject(), new ErrorResponse(statusCode, error).toJson());
                } else {
                    context.assertNull(res.bodyAsJsonObject());
                }
                return Future.succeededFuture();
            });
    }

    protected Future<HttpResponse<Buffer>> sendPOSTMessage(TestContext context, WebClient client, JsonObject requestBody) {
        Promise<HttpResponse<Buffer>> response = Promise.promise();
        client
            .post(DEFAULT_SERVER_PORT, DEFAULT_HOST, CLIENT_URI)
            .sendJson(requestBody, context.asyncAssertSuccess(response::complete));
        return response.future();
    }
}
