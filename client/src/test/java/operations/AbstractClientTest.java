package operations;

import io.julian.server.components.Configuration;
import io.julian.server.components.Server;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import java.io.File;

public class AbstractClientTest {
    Server server;
    HttpServer api;
    Vertx vertx;

    protected void setUpApiServer(final TestContext context) {
        server = new Server();
        Future<Boolean> hasDeployed = server.startServer(vertx, System.getProperty("user.dir") + File.separator + ".."  + File.separator + "server" + File.separator + Configuration.DEFAULT_OPENAPI_SPEC_LOCATION);
        api = vertx.createHttpServer(new HttpServerOptions()
            .setPort(Configuration.DEFAULT_SERVER_PORT)
            .setHost(Configuration.DEFAULT_SERVER_HOST));

        Async async = context.async();
        hasDeployed.onComplete(context.asyncAssertSuccess(v -> {
            api.requestHandler(server.getRouterFactory().getRouter()).listen(ar -> {
                context.assertTrue(ar.succeeded());
                async.complete();
            });
        }));
        async.awaitSuccess();
    }

    protected void tearDownAPIServer(final TestContext context) {
        api.close(context.asyncAssertSuccess());
        server = null;
        api = null;
    }
}
