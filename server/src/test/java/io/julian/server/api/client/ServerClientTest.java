package io.julian.server.api.client;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.control.AbstractServerHandlerTest;
import io.julian.server.models.control.OtherServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.response.LabelResponse;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import static io.julian.server.models.coordination.CoordinationMessageTest.JSON;

public class ServerClientTest extends AbstractServerHandlerTest {
    public static final OtherServerConfiguration OTHER_SERVER_CONFIGURATION =
        new OtherServerConfiguration(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT);
    private final String connectionRefusedFormat = "Connection refused: %s/127.0.0.1:%s";

    @Test
    public void TestServerClientSuccessfullySendCoordinateMessage(final TestContext context) {
        setUpApiServer(context);
        ServerClient client = new ServerClient(vertx);
        client.sendCoordinateMessageToServer(OTHER_SERVER_CONFIGURATION, CoordinationMessage.fromJson(JSON))
            .onComplete(context.asyncAssertSuccess(context::assertNull));
    }

    @Test
    public void TestServerClientUnsuccessfullySendCoordinateMessageWhenNoServer(final TestContext context) {
        ServerClient client = new ServerClient(vertx);
        client.sendCoordinateMessageToServer(OTHER_SERVER_CONFIGURATION, CoordinationMessage.fromJson(JSON))
            .onComplete(context.asyncAssertFailure(res -> context.assertEquals(
                String.format(connectionRefusedFormat, OTHER_SERVER_CONFIGURATION.getHost(), OTHER_SERVER_CONFIGURATION.getPort()), res.getMessage())));
    }

    @Test
    public void TestServerClientSuccessfullySendLabel(final TestContext context) {
        setUpApiServer(context);
        String newLabel = "label";
        ServerClient client = new ServerClient(vertx);
        OtherServerConfiguration originalServerConfig = new OtherServerConfiguration(OTHER_SERVER_CONFIGURATION.getHost(), OTHER_SERVER_CONFIGURATION.getPort());
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
        ServerClient client = new ServerClient(vertx);
        OtherServerConfiguration originalServerConfig = new OtherServerConfiguration(OTHER_SERVER_CONFIGURATION.getHost(), OTHER_SERVER_CONFIGURATION.getPort());

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
        ServerClient client = new ServerClient(vertx);
        client.getServerLabel(OTHER_SERVER_CONFIGURATION)
            .onComplete(context.asyncAssertFailure(res -> context.assertEquals(
                String.format(connectionRefusedFormat, OTHER_SERVER_CONFIGURATION.getHost(), OTHER_SERVER_CONFIGURATION.getPort()), res.getMessage())));
    }

    @Test
    public void TestServerClientCanNotSendLabelWhenNoServer(final TestContext context) {
        ServerClient client = new ServerClient(vertx);
        client.sendLabelToServer(OTHER_SERVER_CONFIGURATION, "random-label")
            .onComplete(context.asyncAssertFailure(res -> context.assertEquals(
                String.format(connectionRefusedFormat, OTHER_SERVER_CONFIGURATION.getHost(), OTHER_SERVER_CONFIGURATION.getPort()), res.getMessage())));
    }
}
