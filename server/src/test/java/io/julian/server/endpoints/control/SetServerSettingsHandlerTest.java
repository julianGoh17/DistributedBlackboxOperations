package io.julian.server.endpoints.control;

import io.julian.server.components.Controller;
import io.julian.server.models.ServerStatus;
import io.julian.server.models.control.ServerSettings;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

public class SetServerSettingsHandlerTest extends AbstractServerHandlerTest {
    @Test
    public void TestSuccessfulPostMessageShouldUpdateAllServerSettings(final TestContext context) {
        ServerStatus expectedStatus = ServerStatus.PROBABILISTIC_FAILURE;
        float failureChance = 0.5f;
        setUpApiServer(context);

        WebClient client = WebClient.create(this.vertx);
        Async async = context.async();
        POSTSuccessfulServerSettings(context, client, new ServerSettings(expectedStatus, failureChance), expectedStatus, failureChance)
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context);
    }

    @Test
    public void TestSuccessfulPostMessageShouldOnlyUpdateServerState(final TestContext context) {
        ServerStatus expectedStatus = ServerStatus.UNREACHABLE;
        setUpApiServer(context);

        WebClient client = WebClient.create(this.vertx);
        Async async = context.async();
        POSTSuccessfulServerSettings(context, client, new ServerSettings(expectedStatus, null), expectedStatus,
            Controller.DEFAULT_MESSAGE_FAILURE_CHANCE)
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context);
    }

    @Test
    public void TestSuccessfulPostMessageShouldOnlyUpdateServerFailureChance(final TestContext context) {
        String stringStatus = "random-status";
        ServerStatus status = ServerStatus.forValue(stringStatus);
        float expectedFailureChance = 0.1f;
        setUpApiServer(context);

        WebClient client = WebClient.create(this.vertx);
        Async async = context.async();
        POSTSuccessfulServerSettings(context, client, new ServerSettings(status, expectedFailureChance), ServerStatus.AVAILABLE,
            expectedFailureChance)
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context);
    }
}
