package tools;

import io.julian.gossip.components.State;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Configuration;
import io.julian.server.components.MessageStore;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.concurrent.ConcurrentLinkedQueue;

@RunWith(VertxUnitRunner.class)
public abstract class AbstractHandlerTest {
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

    protected TestServerComponents setUpGossipApiServer(final TestContext context, final ServerConfiguration configuration) {
        TestServerComponents components = new TestServerComponents();
        components.setUpGossipServer(context, vertx, configuration);
        return components;
    }

    protected TestServerComponents setUpBasicApiServer(final TestContext context) {
        TestServerComponents components = new TestServerComponents();
        components.setUpBasicServer(context, vertx, AbstractHandlerTest.DEFAULT_SEVER_CONFIG);
        return components;
    }

    protected void tearDownServer(final TestContext context, final TestServerComponents components) {
        components.tearDownServer(context, vertx);
    }

    protected RegistryManager createTestRegistryManager() {
        RegistryManager manager = new RegistryManager(new Configuration());
        manager.registerServer(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT);
        return manager;
    }

    protected ServerClient createServerClient() {
        return new ServerClient(this.vertx, new Configuration());
    }

    protected TestMetricsCollector setUpMetricsCollector(final TestContext context) {
        TestMetricsCollector metricsCollector = new TestMetricsCollector();
        metricsCollector.setUpMetricsCollector(METRICS_COLLECTOR_CONFIG, context, vertx);
        return metricsCollector;
    }

    protected State createState() {
        return createState(new MessageStore(), new ConcurrentLinkedQueue<>());
    }

    protected State createState(final MessageStore messageStore) {
        return createState(messageStore, new ConcurrentLinkedQueue<>());
    }

    protected State createState(final ConcurrentLinkedQueue<CoordinationMessage> deadLetters) {
        return createState(new MessageStore(), deadLetters);
    }

    protected State createState(final MessageStore store, final ConcurrentLinkedQueue<CoordinationMessage> deadLetters) {
        return new State(store, deadLetters);
    }
}
