package tools;

import io.julian.metrics.collector.server.Configuration;
import io.julian.metrics.collector.server.Server;
import io.julian.server.models.control.ServerConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;

import java.io.File;

public class TestMetricsCollector {
    public Server server;
    public HttpServer api;

    public static final String OPENAPI_SPEC_LOCATION = System.getProperty("user.dir") + File.separator + "../metrics-collector" + File.separator + Configuration.DEFAULT_OPENAPI_SPEC_LOCATION;

    public void setUpMetricsCollector(final ServerConfiguration configuration, final TestContext context, final Vertx vertx) {
        server = new Server(new Configuration());
        Async async = context.async();
        api = vertx.createHttpServer(new HttpServerOptions()
            .setPort(configuration.getPort())
            .setHost(configuration.getHost()));

        server.startServer(vertx, OPENAPI_SPEC_LOCATION)
            .onComplete(context.asyncAssertSuccess(compositeFuture -> api.requestHandler(server.getRouterFactory().getRouter()).listen(ar -> {
                context.assertTrue(ar.succeeded());
                async.complete();
            })));

        async.awaitSuccess();
    }

    public void tearDownMetricsCollector(final TestContext context) {
        server = null;
        Async async = context.async();
        api.close(context.asyncAssertSuccess(v ->  async.complete()));
        async.awaitSuccess();
    }

    public void testHasExpectedStatusSize(final int expectedSize) {
        Assert.assertEquals(expectedSize, server.getTracker().getStatuses().size());
    }
}
