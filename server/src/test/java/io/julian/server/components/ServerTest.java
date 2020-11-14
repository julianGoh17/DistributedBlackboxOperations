package io.julian.server.components;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ServerTest {
    @Test
    public void TestServerFailsPromiseWhenInvalidFileLocation(final TestContext context) {
        String incorrectFilePath = "/incorrect/location";
        Server server = new Server();
        Vertx vertx = Vertx.vertx();
        Promise<Boolean> failedPromise = server.startServer(vertx, incorrectFilePath);
        failedPromise.future().onComplete(context.asyncAssertFailure(fail -> Assert.assertEquals(String.format("Wrong specification url/path: %s", incorrectFilePath), fail.getMessage())));
    }
}
