package io.julian.integration;

import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class IntegrationTest extends AbstractServerBaseTest {
    private TestClient client;
    private final static JsonObject TEST_MESSAGE = new JsonObject().put("Test", "Key");

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
        client = new TestClient(vertx);
    }

    @After
    public void after() {
        this.vertx.close();
    }

    @Test
    public void TestCanSendMessageInToServerAndMessageIsDuplicated(final TestContext context) {
        setUpApiServer(context);
        Async async = context.async();
        client.POST_COORDINATE_MESSAGE(TestClient.MESSAGE)
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertEquals(200, res.statusCode());
                Assert.assertEquals(2, server.getController().getNumberOfCoordinationMessages());

                for (int i = 0; i < 2; i++) {
                    TestCoordinateMessagesAreTheSame(TestClient.MESSAGE, server.getController().getCoordinationMessage());
                }

                Assert.assertEquals(0, server.getController().getNumberOfCoordinationMessages());
                async.complete();
            }));

        async.await();
        tearDownServer(context);
    }

    @Test
    public void TestPOSTMessageAppearsInClientQueueInServer(final TestContext context) {
        setUpApiServer(context);
        Async async = context.async();
        client.POST_MESSAGE(TEST_MESSAGE)
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertEquals(200, res.statusCode());
                Assert.assertEquals(1, server.getController().getNumberOfClientMessages());
                final ClientMessage clientMessage = server.getController().getClientMessage();
                Assert.assertEquals(TEST_MESSAGE.encodePrettily(),
                    clientMessage.getMessage().encodePrettily());
                Assert.assertEquals(HTTPRequest.POST, clientMessage.getRequest());

                Assert.assertEquals(0, server.getController().getNumberOfClientMessages());
                async.complete();
            }));

        async.await();
        tearDownServer(context);
    }

    @Test
    public void TestDELETEMessageAppearsInClientQueueInServer(final TestContext context) {
        setUpApiServer(context);
        Async async = context.async();
        client.POST_MESSAGE(TEST_MESSAGE)
            .compose(res -> {
                Assert.assertEquals(200, res.statusCode());
                Assert.assertEquals(1, server.getController().getNumberOfClientMessages());

                final ClientMessage clientMessage = server.getController().getClientMessage();
                Assert.assertEquals(TEST_MESSAGE.encodePrettily(),
                    clientMessage.getMessage().encodePrettily());
                Assert.assertEquals(HTTPRequest.POST, clientMessage.getRequest());
                Assert.assertEquals(0, server.getController().getNumberOfClientMessages());

                return Future.succeededFuture(res.bodyAsJsonObject().getString("messageId"));
            })
            .compose(id -> client.DELETE_MESSAGE(id))
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertEquals(200, res.statusCode());
                Assert.assertEquals(1, server.getController().getNumberOfClientMessages());

                final ClientMessage clientMessage = server.getController().getClientMessage();
                Assert.assertNull(clientMessage.getMessage());
                Assert.assertEquals(HTTPRequest.DELETE, clientMessage.getRequest());
                Assert.assertEquals(0, server.getController().getNumberOfClientMessages());
                async.complete();
            }));

        async.await();
        tearDownServer(context);
    }

    public void TestCoordinateMessagesAreTheSame(final CoordinationMessage expected, final CoordinationMessage found) {
        Assert.assertEquals(expected.getMetadata().getTimestamp().toValue(), found.getMetadata().getTimestamp().toValue());
        Assert.assertEquals(expected.getMetadata().getFromServerId(), found.getMetadata().getFromServerId());

        Assert.assertEquals(expected.getMessage().encodePrettily(), expected.getMessage().encodePrettily());
        Assert.assertEquals(expected.getDefinition().encodePrettily(), expected.getDefinition().encodePrettily());
    }
}
