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
}
