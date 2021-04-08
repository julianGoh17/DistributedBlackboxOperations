package io.julian.server.endpoints;

import io.julian.metrics.collector.server.Configuration;
import io.julian.metrics.collector.server.Server;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;

public class TestMetricsCollector {
    public static final String HOST = Configuration.DEFAULT_SERVER_HOST;
    public static final int PORT = Configuration.DEFAULT_SERVER_PORT;
    public static final String OPENAPI_SPEC_LOCATION = String.format("%s/../metrics-collector/src/main/resources/metrics-collector-endpoints.yaml", System.getProperty("user.dir"));
    public static final String FAILED_TO_CONNECT = String.format("Connection refused: %s/127.0.0.1:%d", HOST, PORT);

    public Server metricsCollector;
    public HttpServer api;

    public void setUpMetricsCollector(final TestContext context, final Vertx vertx) {
        metricsCollector = new Server(new Configuration());
        api = vertx.createHttpServer(new HttpServerOptions()
            .setPort(PORT)
            .setHost(HOST));

        Async async = context.async();
        metricsCollector.startServer(vertx, OPENAPI_SPEC_LOCATION)
            .onComplete(context.asyncAssertSuccess(compositeFuture -> api.requestHandler(metricsCollector.getRouterFactory().getRouter()).listen(ar -> {
                context.assertTrue(ar.succeeded());
                async.complete();
            })));
        async.awaitSuccess();
    }

    public void tearDownServer(final TestContext context) {
        metricsCollector = null;
        Async async = context.async();
        api.close(context.asyncAssertSuccess(v ->  async.complete()));
        async.awaitSuccess();
    }

    public void assertHasNumberOfTrackedMessages(final int messages) {
        Assert.assertEquals(messages, metricsCollector.getTracker().getStatuses().size());
    }
}
