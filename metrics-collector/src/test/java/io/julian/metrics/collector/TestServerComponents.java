package io.julian.metrics.collector;

import io.julian.metrics.collector.server.Server;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import java.io.File;

public class TestServerComponents {
    public Server server;
    public HttpServer api;

    public static final String OPENAPI_SPEC_LOCATION = System.getProperty("user.dir") + File.separator + Server.DEFAULT_OPENAPI_SPEC_LOCATION;
    public static final String HOST = "localhost";
    public static final int PORT = 9999;

    protected void setUpServer(final TestContext context, final Vertx vertx) {
        server = new Server();
        Async async = context.async();
        api = vertx.createHttpServer(new HttpServerOptions()
            .setPort(PORT)
            .setHost(HOST));

        server.startServer(vertx, OPENAPI_SPEC_LOCATION)
            .onComplete(context.asyncAssertSuccess(compositeFuture -> {
                api.requestHandler(server.getRouterFactory().getRouter()).listen(ar -> {
                    context.assertTrue(ar.succeeded());
                    async.complete();
                });
            }));

        async.awaitSuccess();
    }

    protected void tearDownServer(final TestContext context) {
        server = null;
        Async async = context.async();
        api.close(context.asyncAssertSuccess(v ->  async.complete()));
        async.awaitSuccess();
    }
}
