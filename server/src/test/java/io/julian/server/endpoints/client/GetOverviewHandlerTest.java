package io.julian.server.endpoints.client;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.control.AbstractServerHandlerTest;
import io.julian.server.models.response.ServerOverview;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

import java.util.ArrayList;

public class GetOverviewHandlerTest extends AbstractServerHandlerTest {
    @Test
    public void TestCanGetOverviewSuccessfully(final TestContext context) {
        setUpApiServer(context);
        WebClient client = WebClient.create(this.vertx);

        Async async = context.async();
        sendSuccessfulGETOverview(context, client, new ServerOverview(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT, 0, new ArrayList<>()))
            .onComplete(v -> async.complete());
        async.awaitSuccess();
        tearDownServer(context);
    }
}
