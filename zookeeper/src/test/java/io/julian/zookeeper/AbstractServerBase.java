package io.julian.zookeeper;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Configuration;
import io.julian.server.models.control.ServerConfiguration;
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

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @After
    public void after() {
        this.vertx.close();
    }

    protected TestServerComponents setUpApiServer(final TestContext context, final ServerConfiguration configuration) {
        TestServerComponents components = new TestServerComponents();
        components.setUpServer(context, vertx, configuration);
        return components;
    }

    protected void tearDownServer(final TestContext context, final TestServerComponents components) {
        components.tearDownServer(context, vertx);
    }

    protected RegistryManager createTestRegistryManager() {
        RegistryManager manager = new RegistryManager();
        manager.registerServer(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT);
        return manager;
    }

    protected ServerClient createServerClient() {
        return new ServerClient(this.vertx);
    }
}
