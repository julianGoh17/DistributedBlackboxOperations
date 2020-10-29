package operations;

import io.julian.client.operations.BaseClient;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class BaseClientTest extends AbstractClientTest {
    BaseClient baseClient;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
        baseClient = new BaseClient(vertx);
    }

    @After
    public void tearDown() {
        baseClient.closeClient();
        vertx.close();
    }

    @Test
    public void TestSuccessfulPostMessage(TestContext context) {
        setUpApiServer(context);
        JsonObject message = new JsonObject()
            .put("this", "message");

        baseClient.POSTMessage(message)
            .onComplete(context.asyncAssertSuccess(context::assertNotNull));
    }

    @Test
    public void TestSuccessfulGetMessage(TestContext context) {
        setUpApiServer(context);
        JsonObject message = new JsonObject().put("this", "message");

        baseClient.POSTMessage(message)
            .compose(baseClient::GETMessage)
            .onComplete(context.asyncAssertSuccess(res -> context.assertEquals(message, res)));
    }

    @Test
    public void TestUnsuccessfulGetMessage(TestContext context) {
        setUpApiServer(context);
        String randomId = "random-id";

        baseClient.GETMessage(randomId)
            .onComplete(context.asyncAssertFailure(err -> context.assertEquals(String.format("Could not find entry for uuid '%s'", randomId), err.getMessage())));
    }

    @Test
    public void TestSuccessfulPUTMessage(TestContext context) {
        setUpApiServer(context);
        JsonObject originalMessage = new JsonObject().put("original", "message");
        JsonObject newMessage = new JsonObject().put("new", "message");

        baseClient.POSTMessage(originalMessage)
            .compose(id -> baseClient.GETMessage(id)
                .compose(returnedMessage -> {
                    context.assertEquals(originalMessage, returnedMessage);
                    return Future.succeededFuture(id);
                })
            )
            .compose(id -> baseClient.PUTMessage(id, newMessage))
            .compose(id -> baseClient.GETMessage(id).onComplete(context.asyncAssertSuccess(res -> {
                context.assertNotEquals(originalMessage, res);
                context.assertEquals(newMessage, res);
            })));

    }

    @Test
    public void TestUnsuccessfulPUTMessage(TestContext context) {
        setUpApiServer(context);
        JsonObject message = new JsonObject().put("new", "message");
        String nonExistentId = "random-id";

        baseClient.PUTMessage(nonExistentId, message)
            .onComplete(context.asyncAssertFailure(throwable ->
                context.assertEquals(String.format("Could not find entry for uuid '%s'", nonExistentId), throwable.getMessage())));
    }
}
