package io.julian.server.components;

import io.julian.server.endpoints.ServerComponents;
import io.julian.server.models.DistributedAlgorithmSettings;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ServerTest {
    public static final String PACKAGE_NAME = "io.julian.ExampleDistributedAlgorithm";
    public static final String TEST_JAR_PATH = String.format("%s/../test/target/test-1.0-SNAPSHOT-jar-with-dependencies.jar", System.getProperty("user.dir"));

    private Vertx vertx;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @Test
    public void TestServerFailsPromiseWhenInvalidFileLocation(final TestContext context) {
        String incorrectFilePath = "/incorrect/location";
        Server server = new Server();
        Promise<Boolean> failedPromise = server.startServer(vertx, incorrectFilePath);
        failedPromise.future().onComplete(context.asyncAssertFailure(fail -> Assert.assertEquals(String.format("Wrong specification url/path: %s", incorrectFilePath), fail.getMessage())));
    }

    @Test
    public void TestServerDoesNotDeployJarWhenNoTestJarEnvInstantiated(final TestContext context) {
        Server server = new Server();
        server
            .deployDistributedAlgorithmVerticle(server.getController(), vertx, new DistributedAlgorithmSettings(false, true, "", ""))
            .onComplete(context.asyncAssertSuccess(string -> Assert.assertEquals(
                String.format("Skipping loading distributed algorithm because environmental variable '%s' or '%s' not instantiated", Configuration.JAR_FILE_PATH_ENV, Configuration.PACKAGE_NAME_ENV),
                string
            )));
    }

    @Test
    public void TestServerDoesNotDeployJarWhenNoPackageNameEnvInstantiated(final TestContext context) {
        Server server = new Server();
        server
            .deployDistributedAlgorithmVerticle(server.getController(), vertx, new DistributedAlgorithmSettings(true, false, "", ""))
            .onComplete(context.asyncAssertSuccess(string -> Assert.assertEquals(
                String.format("Skipping loading distributed algorithm because environmental variable '%s' or '%s' not instantiated", Configuration.JAR_FILE_PATH_ENV, Configuration.PACKAGE_NAME_ENV),
                string
            )));
    }

    @Test
    public void TestServerDoesNotDeployJarFilePathIsWrong(final TestContext context) {
        Server server = new Server();
        String wrongFilePath = String.format("%s-123", TEST_JAR_PATH);
        server
            .deployDistributedAlgorithmVerticle(server.getController(), vertx, new DistributedAlgorithmSettings(true, true, wrongFilePath, ""))
            .onComplete(context.asyncAssertFailure(string -> Assert.assertEquals(
                String.format("Could not find JAR file at path '%s'", wrongFilePath),
                string.getLocalizedMessage()
            )));
    }

    @Test
    public void TestServerDoesNotDeployPackNameIsWrong(final TestContext context) {
        Server server = new Server();
        String wrongPackageName = PACKAGE_NAME + ".random";
        server
            .deployDistributedAlgorithmVerticle(server.getController(), vertx, new DistributedAlgorithmSettings(true, true, TEST_JAR_PATH, wrongPackageName))
            .onComplete(context.asyncAssertFailure(string -> Assert.assertEquals(
                wrongPackageName,
                string.getMessage()
            )));
    }

    @Test
    public void TestServerCanDeployIfCorrectVariablesGiven(final TestContext context) {
        Server server = new Server();
        server
            .deployDistributedAlgorithmVerticle(server.getController(), vertx, new DistributedAlgorithmSettings(true, true, TEST_JAR_PATH, PACKAGE_NAME))
            .onComplete(context.asyncAssertSuccess(vertx::undeploy));
    }

    @Test
    public void TestServerGetServerComponents() {
        Server server = new Server();

        ServerComponents components = server.createServerComponents(this.vertx);
        Assert.assertNotNull(components.messageStore);
        Assert.assertNotNull(components.vertx);
        Assert.assertNotNull(components.controller);
    }
}
