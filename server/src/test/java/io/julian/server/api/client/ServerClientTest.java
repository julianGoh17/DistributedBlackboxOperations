package io.julian.server.api.client;

import io.julian.metrics.collector.models.TrackedMessage;
import io.julian.server.components.Configuration;
import io.julian.server.endpoints.TestMetricsCollector;
import io.julian.server.endpoints.control.AbstractServerHandlerTest;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.response.LabelResponse;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import static io.julian.server.models.coordination.CoordinationMessageTest.JSON;

public class ServerClientTest extends AbstractServerHandlerTest {
    public static final ServerConfiguration OTHER_SERVER_CONFIGURATION =
        new ServerConfiguration(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT);
    private final String connectionRefusedFormat = "Connection refused: %s/127.0.0.1:%s";

    @Test
    public void TestServerClientSuccessfullySendCoordinateMessage(final TestContext context) {
        setUpApiServer(context);
        ServerClient client = new ServerClient(vertx, new Configuration());
        client.sendCoordinateMessageToServer(OTHER_SERVER_CONFIGURATION, CoordinationMessage.fromJson(JSON))
            .onComplete(context.asyncAssertSuccess(context::assertNull));
    }

    @Test
    public void TestServerClientUnsuccessfullySendCoordinateMessageWhenNoServer(final TestContext context) {
        ServerClient client = new ServerClient(vertx, new Configuration());
        client.sendCoordinateMessageToServer(OTHER_SERVER_CONFIGURATION, CoordinationMessage.fromJson(JSON))
            .onComplete(context.asyncAssertFailure(res -> context.assertEquals(
                String.format(connectionRefusedFormat, OTHER_SERVER_CONFIGURATION.getHost(), OTHER_SERVER_CONFIGURATION.getPort()), res.getMessage())));
    }

    @Test
    public void TestServerClientSuccessfullySendLabel(final TestContext context) {
        setUpApiServer(context);
        String newLabel = "label";
        ServerClient client = new ServerClient(vertx, new Configuration());
        ServerConfiguration originalServerConfig = new ServerConfiguration(OTHER_SERVER_CONFIGURATION.getHost(), OTHER_SERVER_CONFIGURATION.getPort());
        context.assertNotEquals(newLabel, originalServerConfig.getLabel());
        Async async = context.async();
        client.sendLabelToServer(originalServerConfig, newLabel)
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertNull(res);
                context.assertEquals(newLabel, originalServerConfig.getLabel());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestServerClientSuccessfullyGetLabel(final TestContext context) {
        setUpApiServer(context);
        String newLabel = "label";
        server.getController().setLabel(newLabel);
        ServerClient client = new ServerClient(vertx, new Configuration());
        ServerConfiguration originalServerConfig = new ServerConfiguration(OTHER_SERVER_CONFIGURATION.getHost(), OTHER_SERVER_CONFIGURATION.getPort());

        context.assertNotEquals(newLabel, originalServerConfig.getLabel());
        context.assertEquals(newLabel, server.getController().getLabel());
        Async async = context.async();
        client.getServerLabel(originalServerConfig)
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(new LabelResponse(newLabel).toJson(), res.toJson());
                context.assertEquals(newLabel, originalServerConfig.getLabel());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestServerClientUnsuccessfullyGetCoordinateMessageWhenNoServer(final TestContext context) {
        ServerClient client = new ServerClient(vertx, new Configuration());
        client.getServerLabel(OTHER_SERVER_CONFIGURATION)
            .onComplete(context.asyncAssertFailure(res -> context.assertEquals(
                String.format(connectionRefusedFormat, OTHER_SERVER_CONFIGURATION.getHost(), OTHER_SERVER_CONFIGURATION.getPort()), res.getMessage())));
    }

    @Test
    public void TestServerClientCanNotSendLabelWhenNoServer(final TestContext context) {
        ServerClient client = new ServerClient(vertx, new Configuration());
        client.sendLabelToServer(OTHER_SERVER_CONFIGURATION, "random-label")
            .onComplete(context.asyncAssertFailure(res -> context.assertEquals(
                String.format(connectionRefusedFormat, OTHER_SERVER_CONFIGURATION.getHost(), OTHER_SERVER_CONFIGURATION.getPort()), res.getMessage())));
    }

    @Test
    public void TestServerClientCanSendTrackMessageToMetricsCollector(final TestContext context) {
        TestMetricsCollector collector = createCollector(context);
        ServerClient client = new ServerClient(vertx, new Configuration());
        Async async = context.async();
        client.trackMessage(new TrackedMessage(200, "random-id", 10.4f))
            .onComplete(context.asyncAssertSuccess(res -> async.complete()));
        async.awaitSuccess();
        collector.assertHasNumberOfTrackedMessages(1);
        collector.tearDownServer(context);
    }

    @Test
    public void TestServerClientFailsToSendTrackMessageToMetricsCollector(final TestContext context) {
        ServerClient client = new ServerClient(vertx, new Configuration());
        Async async = context.async();
        client.trackMessage(new TrackedMessage(200, "random-id", 10.4f))
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(TestMetricsCollector.FAILED_TO_CONNECT, cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestServerClientFailsToSendTrackMessageWhenMissingFiledToMetricsCollector(final TestContext context) {
        TestMetricsCollector collector = createCollector(context);
        ServerClient client = new ServerClient(vertx, new Configuration());
        Async async = context.async();
        client.trackMessage(new TrackedMessage(200, null, 10.4f))
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals("$.messageId: null found, string expected", cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
        collector.tearDownServer(context);
    }

    @Test
    public void TestServerClientCanSendCreateReportToMetricsCollector(final TestContext context) {
        TestMetricsCollector collector = createCollector(context);
        ServerClient client = new ServerClient(vertx, new Configuration());
        Async async = context.async();
        client.createReport()
            .onComplete(context.asyncAssertSuccess(res -> async.complete()));
        async.awaitSuccess();
        collector.assertFileReportDoesExists(context, vertx, true);
        collector.deleteReportFile(context, vertx);
        collector.tearDownServer(context);
    }

    @Test
    public void TestServerClientFailsToConnectWhenCreatingReportToMetricsCollector(final TestContext context) {
        ServerClient client = new ServerClient(vertx, new Configuration());
        Async async = context.async();
        client.createReport()
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(TestMetricsCollector.FAILED_TO_CONNECT, cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestServerClientFailsWhenCreatingReportToMetricsCollector(final TestContext context) {
        TestMetricsCollector collector = createCollectorWithWrongReportPath(context);
        ServerClient client = new ServerClient(vertx, new Configuration());
        Async async = context.async();
        client.createReport()
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(String.format("java.nio.file.NoSuchFileException: %s", TestMetricsCollector.INVALID_REPORT_FILE_PATH), cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
        collector.assertFileReportDoesExists(context, vertx, false);
        collector.tearDownServer(context);
    }

    private TestMetricsCollector createCollector(final TestContext context) {
        TestMetricsCollector collector = new TestMetricsCollector();
        collector.setUpMetricsCollector(context, vertx);
        return collector;
    }

    private TestMetricsCollector createCollectorWithWrongReportPath(final TestContext context) {
        TestMetricsCollector collector = new TestMetricsCollector();
        collector.setUpMetricsCollectorWithWrongReportPath(context, vertx);
        return collector;
    }
}
