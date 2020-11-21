package io.julian.integration;

import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.Vertx;
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
        client.POST(TestClient.MESSAGE)
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertEquals(200, res.statusCode());
                Assert.assertEquals(2, server.getController().getNumberOfCoordinationMessages());

                for (int i = 0; i < 2; i++) {
                    TestCoordinateMessagesAreTheSame(TestClient.MESSAGE, server.getController().getCoordinationMessage());
                }

                Assert.assertEquals(0, server.getController().getNumberOfCoordinationMessages());
            }));
    }

    public void TestCoordinateMessagesAreTheSame(final CoordinationMessage expected, final CoordinationMessage found) {
        Assert.assertEquals(expected.getMetadata().getTimestamp().toValue(), found.getMetadata().getTimestamp().toValue());
        Assert.assertEquals(expected.getMetadata().getFromServerId(), found.getMetadata().getFromServerId());

        Assert.assertEquals(expected.getMessage().encodePrettily(), expected.getMessage().encodePrettily());
        Assert.assertEquals(expected.getDefinition().encodePrettily(), expected.getDefinition().encodePrettily());
    }
}
