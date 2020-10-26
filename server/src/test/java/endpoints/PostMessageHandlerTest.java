package endpoints;

import components.Server;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import models.ErrorResponse;
import models.MessageIDResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static components.Server.DEFAULT_HOST;
import static components.Server.DEFAULT_SERVER_PORT;
import static components.Server.OPENAPI_SPEC_LOCATION;

@RunWith(VertxUnitRunner.class)
public class PostMessageHandlerTest {
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

    @Test
    public void TestSuccessfulMessageIsInDatabase(TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        JsonObject message = createPostMessage(new JsonObject().put("test", "message"));
        Promise<String> uuid = Promise.promise();
        client
            .post(DEFAULT_SERVER_PORT, DEFAULT_HOST, PostMessageHandler.URI)
            .sendJson(message, context.asyncAssertSuccess(res -> {
                context.assertNotNull(res);
                context.assertNotNull(res.bodyAsJsonObject().getString(MessageIDResponse.messageIdKey));
                uuid.complete(res.bodyAsJsonObject().getString(MessageIDResponse.messageIdKey));
            }));

        uuid.future().onComplete(id -> {
            context.assertEquals(1, server.getMessages().getNumberOfMessages());
            context.assertEquals(message.getJsonObject("message"), server.getMessages().getMessage(id.result()));
        });

    }

    @Test
    public void TestInvalidMessageRespondsWithError(TestContext context) {
        setUpApiServer(context);

        ErrorResponse expectedError = new ErrorResponse(400, new Exception("$.message: is missing but it is required"));
        WebClient client = WebClient.create(this.vertx);
        client
            .post(DEFAULT_SERVER_PORT, DEFAULT_HOST, PostMessageHandler.URI)
            .sendJson(new JsonObject(), context.asyncAssertSuccess(res -> {
                context.assertNotNull(res);
                context.assertEquals(400, res.statusCode());
                context.assertEquals(expectedError.toJson().encodePrettily(), res.bodyAsString());
            }));
    }

    private void setUpApiServer(TestContext context) {
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

    private JsonObject createPostMessage(JsonObject message) {
        return new JsonObject()
            .put("message", message);
    }
}
