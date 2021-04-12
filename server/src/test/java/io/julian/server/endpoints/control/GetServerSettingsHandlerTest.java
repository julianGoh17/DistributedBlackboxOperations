package io.julian.server.endpoints.control;

import io.julian.server.components.Controller;
import io.julian.server.models.ServerStatus;
import io.julian.server.models.control.ServerSettings;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

public class GetServerSettingsHandlerTest extends AbstractServerHandlerTest {
    @Test
    public void TestGetOnDefaultServer(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        GETAndAssertServerSettings(context, client, Controller.DEFAULT_SERVER_STATUS, Controller.DEFAULT_MESSAGE_FAILURE_CHANCE);
        tearDownServer(context);
    }

    @Test
    public void TestGETOnServerAfterPOSTEvenWhenServerIsUnreachable(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);
        ServerStatus expectedStatus = ServerStatus.UNREACHABLE;
        float failureChance = 0.5f;

        ServerSettings settings = new ServerSettings(expectedStatus, failureChance);

        GETAndAssertServerSettings(context, client, Controller.DEFAULT_SERVER_STATUS, Controller.DEFAULT_MESSAGE_FAILURE_CHANCE)
            .compose(v -> POSTSuccessfulServerSettings(context, client, settings, expectedStatus, failureChance))
            .compose(v -> GETAndAssertServerSettings(context, client, expectedStatus, failureChance));
        tearDownServer(context);
    }
}
