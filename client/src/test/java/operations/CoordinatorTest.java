package operations;

import io.julian.client.operations.Coordinator;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static io.julian.server.components.Server.DEFAULT_HOST;
import static io.julian.server.components.Server.DEFAULT_SERVER_PORT;

@RunWith(VertxUnitRunner.class)
public class CoordinatorTest extends AbstractClientTest {
    Coordinator client;
    private static final String TEST_MESSAGE_FILES_PATH = String.format("%s/src/test/resources/messages", System.getProperty("user.dir"));
    private static final String TEST_OPERATION_FILES_PATH = String.format("%s/src/test/resources/operations", System.getProperty("user.dir"));

    private static final int SEQUENTIAL_OPERATION_CHAIN = 1;
    private static final int PARALLEL_OPERATION_CHAIN = 0;
    private static final int OUT_OF_BOUND_MESSAGE_INDEX = 9999;

    private static final int POST_OPERATION_NUMBER = 0;
    private static final int GET_OPERATION_NUMBER = 1;
    private static final int PUT_OPERATION_NUMBER = 2;

    private static final String CONNECTION_REFUSED_EXCEPTION = String.format("Connection refused: %s/127.0.0.1:%d", DEFAULT_HOST, DEFAULT_SERVER_PORT);
    private static final String OUT_OF_BOUND_EXCEPTION = String.format("No original message with index '%d'", OUT_OF_BOUND_MESSAGE_INDEX);

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @Test
    public void TestCoordinatorCanInitialize(TestContext context) throws Exception {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        Assert.assertEquals(3, client.getMemory().getOriginalMessages().size());
        Assert.assertEquals(2, client.getOperationChains().size());
        Assert.assertNotNull(client.getClient());
    }

    /**
     * HTTP METHODS
     */
    @Test
    public void TestCoordinatorCanPOSTSuccessfully(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.sendPOST(0)
            .onComplete(context.asyncAssertSuccess(Assert::assertNull));
    }

    @Test
    public void TestCoordinatorPOSTFail(TestContext context) throws IOException, NullPointerException {
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.sendPOST(0).onComplete(context.asyncAssertFailure(throwable -> Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, throwable.getMessage())));
    }

    @Test
    public void TestCoordinatorGETSuccessfully(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        int messageNum = 0;
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.sendPOST(messageNum)
            .compose(v -> {
                Assert.assertEquals(1, client.getMemory().getExpectedMapping().size());
                Assert.assertNotNull(client.getMemory().getExpectedMapping().get(messageNum));
                return client.sendGET(messageNum);
            })
            .onComplete(context.asyncAssertSuccess(Assert::assertNotNull));
    }

    @Test
    public void TestCoordinator404OnGET(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        int messageNum = 0;
        String id = "random-id";
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getMemory().associateNumberWithID(messageNum, id);
        client.sendGET(messageNum)
            .onComplete(context.asyncAssertFailure(throwable -> context.assertEquals(String.format("Could not find entry for uuid '%s'", id),
                throwable.getMessage())));
        client.getMemory()
            .getExpectedMapping()
            .remove(messageNum);
    }

    /**
     * SEQUENTIAL OPERATION TESTS
     */
    @Test
    public void TestCoordinatorCanPUTSuccessfully(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        int oldIndex = 0;
        int newIndex = 1;
        client.sendPOST(oldIndex)
            .compose(v -> {
                Assert.assertNotNull(client.getMemory().getExpectedMapping().get(oldIndex));
                Assert.assertNull(client.getMemory().getExpectedMapping().get(newIndex));
                return client.sendPUT(oldIndex, newIndex);
            })
            .compose(v -> client.sendGET(newIndex))
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertNull(client.getMemory().getExpectedMapping().get(oldIndex));
                Assert.assertNotNull(client.getMemory().getExpectedMapping().get(newIndex));
                context.assertEquals(client.getMemory().getOriginalMessage(newIndex), res);
            }));
    }

    @Test
    public void TestCoordinatorCanRunOperationChain(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.runOperationChain(SEQUENTIAL_OPERATION_CHAIN).onComplete(context.asyncAssertSuccess(v -> {
            Assert.assertEquals(3, client.getCollector().getTotalMessages());
            Assert.assertEquals(0, client.getCollector().getFailed());
            Assert.assertEquals(3, client.getCollector().getSucceeded());
        }));
    }

    @Test
    public void TestCoordinatorFailsOnPOSTOperation(TestContext context) throws IOException, NullPointerException {
        client = new Coordinator(vertx);
        server = null;
        api = null;
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.runOperationChain(SEQUENTIAL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(throwable -> {
            Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, throwable.getMessage());
            Assert.assertEquals(1, client.getCollector().getTotalMessages());
            Assert.assertEquals(1, client.getCollector().getFailed());
            Assert.assertEquals(0, client.getCollector().getSucceeded());
        }));
    }

    @Test
    public void TestCoordinatorFailsOnGETOperation(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_CHAIN).getOperations().get(GET_OPERATION_NUMBER).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.runOperationChain(SEQUENTIAL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(throwable -> {
            Assert.assertEquals("Could not find entry for uuid 'null'", throwable.getMessage());
            Assert.assertEquals(2, client.getCollector().getTotalMessages());
            Assert.assertEquals(1, client.getCollector().getFailed());
            Assert.assertEquals(1, client.getCollector().getSucceeded());
        }));
    }

    @Test
    public void TestCoordinatorFailsOnPUTOperation(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        int impossibleMessage = 9999;
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_CHAIN).getOperations().get(PUT_OPERATION_NUMBER).getAction().setMessageNumber(impossibleMessage);
        client.runOperationChain(SEQUENTIAL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(throwable -> {
            Assert.assertEquals("Could not find entry for uuid 'null'", throwable.getMessage());
            Assert.assertEquals(3, client.getCollector().getTotalMessages());
            Assert.assertEquals(1, client.getCollector().getFailed());
            Assert.assertEquals(2, client.getCollector().getSucceeded());
        }));
    }

    @Test
    public void TestCoordinatorCanFailPUT(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        int oldIndex = 0;
        int newIndex = 1;
        client.sendPUT(oldIndex, newIndex)
            .onComplete(context.asyncAssertFailure(throwable -> Assert.assertEquals("Could not find entry for uuid 'null'", throwable.getMessage())));
    }

    /**
     * PARALLEL_TESTS
     */
    @Test
    public void TestCoordinatorCanRunOperationChainInParallel(TestContext context) throws IOException, NullPointerException  {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        int messages = client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().size();
        client.runOperationChain(PARALLEL_OPERATION_CHAIN).onComplete(context.asyncAssertSuccess(v -> {
            Assert.assertEquals(messages, client.getCollector().getTotalMessages());
            Assert.assertEquals(0, client.getCollector().getFailed());
            Assert.assertEquals(messages, client.getCollector().getSucceeded());
        }));
    }

    @Test
    public void TestCoordinatorCanFailOperationChainInParallel(TestContext context) throws IOException, NullPointerException  {
        api = null;
        server = null;
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        int messages = client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().size();
        client.runOperationChain(PARALLEL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(error -> {
            Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, error.getMessage());
            Assert.assertEquals(messages, client.getCollector().getTotalMessages());
            Assert.assertEquals(messages, client.getCollector().getFailed());
            Assert.assertEquals(0, client.getCollector().getSucceeded());
        }));
    }

    @Test
    public void TestCoordinatorFailsIfOnlyOneOperationChainFailsInParallel(TestContext context) throws IOException, NullPointerException  {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().get(1).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        int messages = client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().size();
        client.runOperationChain(PARALLEL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(error -> {
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, error.getMessage());
            Assert.assertEquals(messages, client.getCollector().getTotalMessages());
            Assert.assertEquals(1, client.getCollector().getFailed());
            Assert.assertEquals(2, client.getCollector().getSucceeded());
        }));
    }

    @Test
    public void TestCoordinatorFailsIfMultipleOperationChainFailsInParallel(TestContext context) throws IOException, NullPointerException  {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().get(1).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().get(2).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        int messages = client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().size();
        client.runOperationChain(PARALLEL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(error -> {
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, error.getMessage());
            Assert.assertEquals(messages, client.getCollector().getTotalMessages());
            Assert.assertEquals(2, client.getCollector().getFailed());
            Assert.assertEquals(1, client.getCollector().getSucceeded());
        }));
    }

    /**
     * EXCEPTION TESTS
     */
    @Test
    public void TestCoordinatorThrowsOutOfBoundExceptionOnPOSTOperation(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_CHAIN).getOperations().get(POST_OPERATION_NUMBER).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.runOperationChain(SEQUENTIAL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(throwable -> {
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, throwable.getMessage());
            Assert.assertEquals(1, client.getCollector().getTotalMessages());
            Assert.assertEquals(1, client.getCollector().getFailed());
            Assert.assertEquals(0, client.getCollector().getSucceeded());
        }));
    }

    @Test
    public void TestCoordinatorThrowsOutOfBoundExceptionOnPUTOperation(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_CHAIN).getOperations().get(PUT_OPERATION_NUMBER).getAction().setNewMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.runOperationChain(SEQUENTIAL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(throwable -> {
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, throwable.getMessage());
            Assert.assertEquals(3, client.getCollector().getTotalMessages());
            Assert.assertEquals(1, client.getCollector().getFailed());
            Assert.assertEquals(2, client.getCollector().getSucceeded());
        }));
    }

    @After
    public void tearDown() {
        client.close();
        vertx.close();
    }

    protected void setUpApiServer(TestContext context) {
        super.setUpApiServer(context);
        client = new Coordinator(vertx);
    }
}
