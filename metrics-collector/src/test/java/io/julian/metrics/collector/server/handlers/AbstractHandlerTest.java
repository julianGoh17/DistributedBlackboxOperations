package io.julian.metrics.collector.server.handlers;

import io.julian.metrics.collector.TestClient;
import io.julian.metrics.collector.TestServerComponents;
import io.julian.metrics.collector.report.ReportCreatorTest;
import io.julian.metrics.collector.server.Configuration;
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
        Configuration configuration = new Configuration();
        configuration.setReportPath(ReportCreatorTest.REPORT_LOCATION);
        server.setUpServer(configuration, context, vertx);
        return server;
    }

    protected TestServerComponents startServer(final Configuration configuration, final TestContext context) {
        TestServerComponents server = new TestServerComponents();
        server.setUpServer(configuration, context, vertx);
        return server;
    }

    protected TestClient createTestClient() {
        return new TestClient(vertx);
    }
}
