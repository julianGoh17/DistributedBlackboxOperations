package io.julian.zookeeper;

import io.julian.TestMetricsCollector;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Configuration;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.models.CandidateInformation;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public abstract class AbstractServerBase {
    protected Vertx vertx;

    public static final ServerConfiguration DEFAULT_SEVER_CONFIG = new ServerConfiguration(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT);
    public static final ServerConfiguration SECOND_SERVER_CONFIG = new ServerConfiguration(Configuration.DEFAULT_SERVER_HOST, 9998);
    public static final ServerConfiguration METRICS_COLLECTOR_CONFIG = new ServerConfiguration(Configuration.DEFAULT_METRICS_COLLECTOR_HOST, Configuration.DEFAULT_METRICS_COLLECTOR_PORT);

    public static final String CONNECTION_REFUSED_EXCEPTION = String.format("Connection refused: %s/127.0.0.1:%d", Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT);

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @After
    public void after() {
        this.vertx.close();
    }

    protected TestServerComponents setUpZookeeperApiServer(final TestContext context, final ServerConfiguration configuration) {
        TestServerComponents components = new TestServerComponents();
        components.setUpZookeeperServer(context, vertx, configuration);
        return components;
    }

    protected TestServerComponents setUpBasicApiServer(final TestContext context, final ServerConfiguration configuration) {
        TestServerComponents components = new TestServerComponents();
        components.setUpBasicServer(context, vertx, configuration);
        return components;
    }

    protected void tearDownServer(final TestContext context, final TestServerComponents components) {
        components.tearDownServer(context, vertx);
    }

    protected CandidateInformationRegistry createTestCandidateInformationRegistry(final boolean doesUpdateLeader) {
        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        registry.addCandidateInformation(new CandidateInformation(DEFAULT_SEVER_CONFIG.getHost(), DEFAULT_SEVER_CONFIG.getPort(), 1));
        if (doesUpdateLeader) {
            registry.updateNextLeader();
        }
        return registry;
    }

    protected RegistryManager createTestRegistryManager() {
        RegistryManager manager = new RegistryManager(new Configuration());
        manager.registerServer(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT);
        return manager;
    }

    protected ServerClient createServerClient() {
        return new ServerClient(this.vertx, new Configuration());
    }

    protected TestClient createTestClient() {
        return new TestClient(this.vertx);
    }

    protected TestMetricsCollector setUpMetricsCollector(final TestContext context) {
        TestMetricsCollector metricsCollector = new TestMetricsCollector();
        metricsCollector.setUpMetricsCollector(METRICS_COLLECTOR_CONFIG, context, vertx);
        return metricsCollector;
    }
}
