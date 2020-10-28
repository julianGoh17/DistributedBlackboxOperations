package operations;

import io.julian.client.operations.Client;
import io.julian.server.components.Server;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static io.julian.server.components.Server.DEFAULT_HOST;
import static io.julian.server.components.Server.DEFAULT_SERVER_PORT;
import static io.julian.server.components.Server.OPENAPI_SPEC_LOCATION;

@RunWith(VertxUnitRunner.class)
public class ClientTest {
    Server server;
    HttpServer api;
    Vertx vertx;
    Client client;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
        client = new Client(vertx);
    }

    @After
    public void tearDown() {
        client.closeClient();
        vertx.close();
    }

    @Test
    public void TestSuccessfulPostMessage(TestContext context) {
        setUpApiServer(context);
        JsonObject message = new JsonObject()
            .put("this", "message");

        client.POSTMessage(message)
            .onComplete(context.asyncAssertSuccess(context::assertNotNull));
    }

    @Test
    public void TestSuccessfulGetMessage(TestContext context) {
        setUpApiServer(context);
        JsonObject message = new JsonObject().put("this", "message");

        client.POSTMessage(message)
            .compose(client::GETMessage)
            .onComplete(context.asyncAssertSuccess(res -> context.assertEquals(message, res)));
    }

    @Test
    public void TestUnsuccessfulGetMessage(TestContext context) {
        setUpApiServer(context);
        String randomId = "random-id";

        client.GETMessage(randomId)
            .onComplete(context.asyncAssertFailure(err -> context.assertEquals(String.format("Could not find entry for uuid '%s'", randomId), err.getMessage())));
    }

    @Test
    public void TestSuccessfulPUTMessage(TestContext context) {
        setUpApiServer(context);
        JsonObject originalMessage = new JsonObject().put("original", "message");
        JsonObject newMessage = new JsonObject().put("new", "message");

        client.POSTMessage(originalMessage)
            .compose(id -> client.GETMessage(id)
                .compose(returnedMessage -> {
                    context.assertEquals(originalMessage, returnedMessage);
                    return Future.succeededFuture(id);
                })
            )
            .compose(id -> client.PUTMessage(id, newMessage))
            .compose(id -> client.GETMessage(id).onComplete(context.asyncAssertSuccess(res -> {
                context.assertNotEquals(originalMessage, res);
                context.assertEquals(newMessage, res);
            })));

    }

    @Test
    public void TestUnsuccessfulPUTMessage(TestContext context) {
        setUpApiServer(context);
        JsonObject message = new JsonObject().put("new", "message");
        String nonExistentId = "random-id";

        client.PUTMessage(nonExistentId, message)
            .onComplete(context.asyncAssertFailure(throwable ->
                context.assertEquals(String.format("Could not find entry for uuid '%s'", nonExistentId), throwable.getMessage())));
    }

    protected void setUpApiServer(TestContext context) {
        server = new Server();
        Promise<Boolean> hasDeployed = server.startServer(vertx, System.getProperty("user.dir") + File.separator + ".."  + File.separator + "server" + File.separator + OPENAPI_SPEC_LOCATION);
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

}
