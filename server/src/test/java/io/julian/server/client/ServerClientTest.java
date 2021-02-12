package io.julian.server.client;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.control.AbstractServerHandlerTest;
import io.julian.server.models.control.OtherServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import static io.julian.server.models.coordination.CoordinationMessageTest.JSON;

public class ServerClientTest extends AbstractServerHandlerTest {
    public static final OtherServerConfiguration OTHER_SERVER_CONFIGURATION =
        new OtherServerConfiguration(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT);
    private final String connectionRefusedFormat = "Connection refused: %s/127.0.0.1:%s";

    @Test
    public void TestServerClientCanSendCoordinateMessage(final TestContext context) {
        setUpApiServer(context);
        ServerClient client = new ServerClient(vertx);
        client.sendCoordinateMessageToServer(OTHER_SERVER_CONFIGURATION, CoordinationMessage.fromJson(JSON))
            .onComplete(context.asyncAssertSuccess(context::assertNull));
    }

    @Test
    public void TestServerClientCanNotSendCoordinateMessageWhenNoServer(final TestContext context) {
        ServerClient client = new ServerClient(vertx);
        client.sendCoordinateMessageToServer(OTHER_SERVER_CONFIGURATION, CoordinationMessage.fromJson(JSON))
            .onComplete(context.asyncAssertFailure(res -> context.assertEquals(
                String.format(connectionRefusedFormat, OTHER_SERVER_CONFIGURATION.getHost(), OTHER_SERVER_CONFIGURATION.getPort()), res.getMessage())));
    }

    @Test
    public void TestServerClientCanSendLabelSuccessfully(final TestContext context) {
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
    public void TestServerClientCanNotSendLabelWhenNoServer(final TestContext context) {
        ServerClient client = new ServerClient(vertx);
        client.sendLabelToServer(OTHER_SERVER_CONFIGURATION, "random-label")
            .onComplete(context.asyncAssertFailure(res -> context.assertEquals(
                String.format(connectionRefusedFormat, OTHER_SERVER_CONFIGURATION.getHost(), OTHER_SERVER_CONFIGURATION.getPort()), res.getMessage())));
    }
}
