package io.julian.metrics.collector.server;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ServerTest {
    private Vertx vertx;

    @Before
    public void Before() {
        vertx = Vertx.vertx();
    }

    @After
    public void After() {
        vertx.close();
    }

    @Test
    public void TestServerCanDeploy(final TestContext context) {
        Async async = context.async();

        Server server = new Server(new Configuration());
        server.startServer(vertx, String.format("%s/%s", System.getProperty("user.dir"), Configuration.DEFAULT_OPENAPI_SPEC_LOCATION))
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
    }

    @Test
    public void TestServerFailsToDeploy(final TestContext context) {
        Async async = context.async();

        String filePath = "incorrect-file-path";
        Server server = new Server(new Configuration());
        server.startServer(vertx, filePath)
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(String.format("Wrong specification url/path: %s", filePath), cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }
}
