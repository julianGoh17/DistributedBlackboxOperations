package io.julian.metrics.collector.server.handlers;

import io.julian.metrics.collector.TestClient;
import io.julian.metrics.collector.TestServerComponents;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public abstract class AbstractHandlerTest {
    protected Vertx vertx;

    @Before
    public void Before() {
        vertx = Vertx.vertx();
    }

    @After
    public void After() {
        vertx.close();
    }

    protected TestServerComponents startServer(final TestContext context) {
        TestServerComponents server = new TestServerComponents();
        server.setUpServer(context, vertx);
        return server;
    }

    protected TestClient createTestClient() {
        return new TestClient(vertx);
    }
}
