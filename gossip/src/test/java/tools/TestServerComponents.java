package tools;

import io.julian.server.components.Configuration;
import io.julian.server.components.Server;
import io.julian.server.models.DistributedAlgorithmSettings;
import io.julian.server.models.control.ServerConfiguration;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class TestServerComponents {
    public Server server;
    public HttpServer api;
    public ServerConfiguration configuration;
    public AtomicReference<String> deploymentID = new AtomicReference<>();

    public static final String GOSSIP_TEST_JAR_PATH = String.format("%s/target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar", System.getProperty("user.dir"));
    public static final String GOSSIP_PACKAGE_NAME = "io.julian.gossip.Gossip";

    public static final String BASIC_SERVER_TEST_JAR_PATH = String.format("%s/../test/target/test-1.0-SNAPSHOT-jar-with-dependencies.jar", System.getProperty("user.dir"));
    public static final String BASIC_SERVER_PACKAGE_NAME = "io.julian.ExampleDistributedAlgorithm";

    protected void setUpGossipServer(final TestContext context, final Vertx vertx, final ServerConfiguration configuration) {
        setUpServer(context, vertx, configuration, GOSSIP_TEST_JAR_PATH, GOSSIP_PACKAGE_NAME);
    }

    protected void setUpBasicServer(final TestContext context, final Vertx vertx, final ServerConfiguration configuration) {
        setUpServer(context, vertx, configuration, BASIC_SERVER_TEST_JAR_PATH, BASIC_SERVER_PACKAGE_NAME);
    }

    protected void setUpServer(final TestContext context, final Vertx vertx, final ServerConfiguration configuration, final String testJar, final String serverPackage) {
        server = new Server();
        Async async = context.async();
        server.getConfiguration().setServerPort(configuration.getPort());
        server.getConfiguration().setServerHost(configuration.getHost());
        api = vertx.createHttpServer(new HttpServerOptions()
            .setPort(configuration.getPort())
            .setHost(configuration.getHost()));
        this.configuration = configuration;

        CompositeFuture.all(
            server.startServer(vertx, System.getProperty("user.dir") + File.separator + ".." + File.separator + "server" + File.separator + Configuration.DEFAULT_OPENAPI_SPEC_LOCATION),
            server.deployDistributedAlgorithmVerticle(server.getController(), vertx, new DistributedAlgorithmSettings(true, true, testJar, serverPackage))
        )
            .onComplete(context.asyncAssertSuccess(compositeFuture -> {
                deploymentID.set(compositeFuture.resultAt(1));
                api.requestHandler(server.getRouterFactory().getRouter()).listen(ar -> {
                    context.assertTrue(ar.succeeded());
                    vertx.setTimer(500, v -> async.complete());
                });
            }));

        async.awaitSuccess();
    }

    protected void tearDownServer(final TestContext context, final Vertx vertx) {
        server = null;
        Async async = context.async();
        api.close(res -> vertx.undeploy(deploymentID.get(), context.asyncAssertSuccess(v ->  async.complete())));
        async.awaitSuccess();
    }
}
