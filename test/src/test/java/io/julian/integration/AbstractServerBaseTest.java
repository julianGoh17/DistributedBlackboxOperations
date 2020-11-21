package io.julian.integration;

import io.julian.server.components.Configuration;
import io.julian.server.components.Server;
import io.julian.server.models.DistributedAlgorithmSettings;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(VertxUnitRunner.class)
public abstract class AbstractServerBaseTest {
    Server server;
    HttpServer api;
    Vertx vertx;

    public static final String CORRECT_TEST_JAR_PATH = String.format("%s/../test/target/test-1.0-SNAPSHOT-jar-with-dependencies.jar", System.getProperty("user.dir"));
    public static final String PACKAGE_NAME = "io.julian.Main";

    protected void setUpApiServer(final TestContext context) {
        server = new Server();
        Async async = context.async();
        api = vertx.createHttpServer(new HttpServerOptions()
            .setPort(Configuration.DEFAULT_SERVER_PORT)
            .setHost(Configuration.DEFAULT_SERVER_HOST));

        CompositeFuture.all(
            server.startServer(vertx, System.getProperty("user.dir") + File.separator + ".." + File.separator + "server" + File.separator + Configuration.DEFAULT_OPENAPI_SPEC_LOCATION).future(),
            server.deployDistributedAlgorithmVerticle(server.getController(), vertx, new DistributedAlgorithmSettings(true, true, CORRECT_TEST_JAR_PATH, PACKAGE_NAME))
        )
            .onComplete(context.asyncAssertSuccess(v ->
                api.requestHandler(server.getRouterFactory().getRouter()).listen(ar -> {
                    context.assertTrue(ar.succeeded());
                    async.complete();
                })));

        async.awaitSuccess();
    }
}
