package operations;

import io.julian.server.components.Server;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import java.io.File;

import static io.julian.server.components.Server.DEFAULT_HOST;
import static io.julian.server.components.Server.DEFAULT_SERVER_PORT;
import static io.julian.server.components.Server.OPENAPI_SPEC_LOCATION;

public class AbstractClientTest {
    Server server;
    HttpServer api;
    Vertx vertx;

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
