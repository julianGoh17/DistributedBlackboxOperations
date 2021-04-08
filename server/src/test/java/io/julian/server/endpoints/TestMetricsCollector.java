package io.julian.server.endpoints;

import io.julian.metrics.collector.report.ReportCreator;
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
    public static final String REPORT_LOCATION = String.format("%s/src/test/resources", System.getProperty("user.dir"));
    public static final String REPORT_FILE_PATH = String.format("%s/%s", REPORT_LOCATION, ReportCreator.REPORT_FILE_NAME);
    public static final String FAILED_TO_CONNECT = String.format("Connection refused: %s/127.0.0.1:%d", HOST, PORT);
    public static final String INVALID_REPORT_LOCATION = "/invalid-path";
    public static final String INVALID_REPORT_FILE_PATH = String.format("%s/%s", INVALID_REPORT_LOCATION, ReportCreator.REPORT_FILE_NAME);

    public Server metricsCollector;
    public HttpServer api;

    public void setUpMetricsCollector(final TestContext context, final Vertx vertx) {
        Configuration configuration = new Configuration();
        configuration.setReportPath(REPORT_LOCATION);
        setUpMetricsCollector(context, configuration, vertx);
    }

    public void setUpMetricsCollectorWithWrongReportPath(final TestContext context, final Vertx vertx) {
        Configuration configuration = new Configuration();
        configuration.setReportPath(INVALID_REPORT_LOCATION);
        setUpMetricsCollector(context, configuration, vertx);
    }

    public void setUpMetricsCollector(final TestContext context, final Configuration configuration, final Vertx vertx) {
        metricsCollector = new Server(configuration);
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

    public void assertFileReportDoesExists(final TestContext context, final Vertx vertx, final boolean doesExist) {
        Async async = context.async();
        vertx.fileSystem().exists(REPORT_FILE_PATH, ar -> {
            Assert.assertEquals(doesExist, ar.result());
            async.complete();
        });
        async.awaitSuccess();
    }

    public void deleteReportFile(final TestContext context, final Vertx vertx) {
        Async async = context.async();
        vertx.fileSystem().exists(REPORT_FILE_PATH, doesExist -> {
            if (doesExist.result()) {
                vertx.fileSystem().delete(REPORT_FILE_PATH, ar -> async.complete());
            }
        });
        async.awaitSuccess();
    }
}
