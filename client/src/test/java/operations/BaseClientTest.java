package operations;

import io.julian.client.model.operation.Expected;
import io.julian.client.operations.BaseClient;
import io.julian.client.operations.ClientConfiguration;
import io.julian.server.components.Configuration;
import io.julian.server.models.control.ServerConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class BaseClientTest extends AbstractClientTest {
    BaseClient baseClient;
    private final Expected expectedSuccess = new Expected(200);
    protected final static ServerConfiguration SERVER_CONFIGURATION = new ServerConfiguration(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT);

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
        baseClient = new BaseClient(vertx, new ClientConfiguration());
    }

    @After
    public void tearDown() {
        baseClient.closeClient();
        vertx.close();
    }

    @Test
    public void TestSuccessfulPostMessage(final TestContext context) {
        setUpApiServer(context);
        JsonObject message = new JsonObject()
            .put("this", "message");

        Async async = context.async();
        baseClient.POSTMessage(message, new Expected(200))
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertNotNull(res);
                async.complete();
            }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestUnsuccessfulPostMessage(final TestContext context) {
        JsonObject message = new JsonObject()
            .put("this", "message");

        Async async = context.async();
        baseClient.POSTMessage(message, expectedSuccess)
            .onComplete(context.asyncAssertFailure(res -> {
                context.assertEquals(
                    expectedSuccess.generateMismatchedException(500, "Connection refused: localhost/127.0.0.1:8888").getMessage(),
                    res.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestUnsuccessfulPostMessagePassesIfExpected(final TestContext context) {
        JsonObject message = new JsonObject()
            .put("this", "message");
        Async async = context.async();

        baseClient.POSTMessage(message, new Expected(500))
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertNull(res);
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestSuccessfulGetMessage(final TestContext context) {
        setUpApiServer(context);
        JsonObject message = new JsonObject().put("this", "message");

        Async async = context.async();
        baseClient.POSTMessage(message, expectedSuccess)
            .compose(id -> baseClient.GETMessage(id, new Expected(200)))
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(message, res);
                async.complete();
            }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestUnsuccessfulGetMessage(final TestContext context) {
        setUpApiServer(context);
        String randomId = "random-id";

        Async async = context.async();
        baseClient.GETMessage(randomId, expectedSuccess)
            .onComplete(context.asyncAssertFailure(err -> {
                context.assertEquals(
                    expectedSuccess.generateMismatchedException(404, String.format("Could not find entry for uuid '%s'", randomId)).getMessage(),
                    err.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestUnsuccessfulGetMessageIfMatchesStatusCode(final TestContext context) {
        String randomId = "random-id";

        Async async = context.async();
        baseClient.GETMessage(randomId, new Expected(500))
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertNull(res);
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestSuccessfulGetOverview(final TestContext context) {
        setUpApiServer(context);

        Async async = context.async();
        baseClient.GetOverview(SERVER_CONFIGURATION)
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertNotNull(res);

                context.assertEquals(0, res.getNumMessages());
                context.assertEquals(Configuration.DEFAULT_SERVER_PORT, res.getPort());
                context.assertEquals(Configuration.DEFAULT_SERVER_HOST, res.getHost());
                async.complete();
            }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestUnsuccessfulGetOverview(final TestContext context) {
        Async async = context.async();
        baseClient.GetOverview(SERVER_CONFIGURATION)
            .onComplete(context.asyncAssertFailure(cause -> {
                context.assertEquals("Connection refused: localhost/127.0.0.1:8888", cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestUnsuccessfulGetMessageIfCannotConnect(final TestContext context) {
        String randomId = "random-id";
        Expected expected = new Expected(200);
        Async async = context.async();
        baseClient.GETMessage(randomId, expected)
            .onComplete(context.asyncAssertFailure(err -> {
                context.assertEquals(expected.generateMismatchedException(500, "Connection refused: localhost/127.0.0.1:8888").getMessage(),
                    err.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestUnsuccessfulDELETEMessageIfMatchesStatusCode(final TestContext context) {
        String randomId = "random-id";

        Async async = context.async();
        baseClient.DELETEMessage(randomId, new Expected(500))
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertNull(res);
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestUnsuccessfulDELETEMessageWhenCannotConnect(final TestContext context) {
        String randomId = "random-id";
        Expected expected = new Expected(200);
        Async async = context.async();
        baseClient.DELETEMessage(randomId, expected)
            .onComplete(context.asyncAssertFailure(err -> {
                context.assertEquals(expected.generateMismatchedException(500, "Connection refused: localhost/127.0.0.1:8888").getMessage(),
                        err.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestSuccessfulGetMessageIfStatusCodeMatch(final TestContext context) {
        setUpApiServer(context);
        String randomId = "random-id";

        Async async = context.async();
        baseClient.GETMessage(randomId, new Expected(404))
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertNull(res);
                async.complete();
            }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestSuccessfulDeleteMessage(final TestContext context) {
        setUpApiServer(context);
        JsonObject message = new JsonObject().put("this", "message");

        Async async = context.async();
        baseClient.POSTMessage(message, new Expected(200))
            .compose(id -> baseClient.DELETEMessage(id, new Expected(200)))
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertNotNull(res);
                async.complete();
            }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestSuccessfulDELETEMessageIfMatchesStatusCode(final TestContext context) {
        setUpApiServer(context);
        String randomId = "random-id";

        Async async = context.async();
        baseClient.DELETEMessage(randomId, new Expected(404))
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertNull(res);
                async.complete();
            }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }
}
