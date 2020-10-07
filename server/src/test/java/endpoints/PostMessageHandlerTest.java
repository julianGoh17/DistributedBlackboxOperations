package endpoints;

import api.Server;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import models.MessageIDResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static api.Server.DEFAULT_HOST;
import static api.Server.DEFAULT_SERVER_PORT;

@RunWith(VertxUnitRunner.class)
public class PostMessageHandlerTest {
    Server server = new Server();
    HttpServer api;
    Vertx vertx;

    @Before
    public void setUpServer() {
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void TestSuccessfulMessageIsInDatabase(TestContext context) {
        Promise<Boolean> hasDeployed = server.startServer(vertx);
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

        WebClient client = WebClient.create(vertx);

        JsonObject message = new JsonObject().put("test", "message");
        Promise<String> uuid = Promise.promise();
        client
            .post(DEFAULT_SERVER_PORT, DEFAULT_HOST, PostMessageHandler.URI)
            .sendJson(message, context.asyncAssertSuccess(res -> {
                context.assertNotNull(res);
                context.assertNotNull(res.bodyAsJsonObject().getString(MessageIDResponse.messageIdKey));
                uuid.complete(res.bodyAsJsonObject().getString(MessageIDResponse.messageIdKey));
            }));

        uuid.future().onComplete(id -> context.assertEquals(message, server.getMessageStore().getMessage(id.result())));
    }

}
