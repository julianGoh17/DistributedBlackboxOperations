package operations;

import io.julian.client.operations.CoordinatorClient;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class CoordinatorClientTest extends AbstractClientTest {
    CoordinatorClient client;
    private static final String TEST_MESSAGE_FILES_PATH = String.format("%s/src/test/resources/messages", System.getProperty("user.dir"));
    private static final String TEST_OPERATION_FILES_PATH = String.format("%s/src/test/resources/operations", System.getProperty("user.dir"));


    @Before
    public void before() {
        this.vertx = Vertx.vertx();
        client = new CoordinatorClient(vertx);
    }

    @Test
    public void TestCoordinatorClientCanInitialize() {
        try {
            client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
            Assert.assertEquals(3, client.getMemory().getOriginalMessages().size());
            Assert.assertEquals(1, client.getOperationChains().size());
            Assert.assertNotNull(client.getClient());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void TestCoordinatorClientCanSendMessage(TestContext context) {
        setUpApiServer(context);
    }

    @After
    public void tearDown() {
        client.close();
        vertx.close();
    }

}
