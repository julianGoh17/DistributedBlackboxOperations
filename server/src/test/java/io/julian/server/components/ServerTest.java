package io.julian.server.components;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ServerTest {
    @Test
    public void TestServerFailsPromiseWhenInvalidFileLocation() {
        Server server = new Server();
        Vertx vertx = Vertx.vertx();
        vertx.close();
        Promise<Boolean> failedPromise = server.startServer(vertx, "/incorrect/location");
        failedPromise.future().onComplete(res -> Assert.assertTrue(res.failed()));
    }

    @Test
    public void TestServerFailsPromiseWhenClosedVertx() {
        Server server = new Server();
        Vertx vertx = Vertx.vertx();
        vertx.close();
        Promise<Boolean> failedPromise = server.startServer(vertx, Server.OPENAPI_SPEC_LOCATION);
        failedPromise.future().onComplete(res -> Assert.assertTrue(res.failed()));
    }
}
