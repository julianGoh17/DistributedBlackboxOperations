package operations;

import io.julian.client.Exception.ClientException;
import io.julian.client.model.MismatchedResponse;
import io.julian.client.model.RequestMethod;
import io.julian.client.model.operation.Operation;
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
        client.runOperationChain(SEQUENTIAL_OPERATION_CHAIN).onComplete(context.asyncAssertSuccess(v ->
            checkCollectorGenericMetrics(1, 1, 1, 0, 0, 0)));
    }

    @Test
    public void TestCoordinatorFailsOnPOSTOperation(TestContext context) throws IOException, NullPointerException {
        client = new Coordinator(vertx);
        server = null;
        api = null;
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.runOperationChain(SEQUENTIAL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(throwable -> {
            ClientException exception = (ClientException) throwable;
            Assert.assertEquals(400, exception.getStatusCode());
            Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(0, 0, 0, 0, 1, 0);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_CHAIN).getOperations().get(POST_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
        }));
    }

    @Test
    public void TestCoordinatorFailsOnGETOperation(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_CHAIN).getOperations().get(GET_OPERATION_NUMBER).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.runOperationChain(SEQUENTIAL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(throwable -> {
            ClientException exception = (ClientException) throwable;
            Assert.assertEquals(404, exception.getStatusCode());
            Assert.assertEquals("Could not find entry for uuid 'null'", exception.getMessage());
            checkCollectorGenericMetrics(0, 1, 0, 1, 0, 0);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_CHAIN).getOperations().get(GET_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
        }));
    }

    @Test
    public void TestCoordinatorFailsOnPUTOperation(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        int impossibleMessage = 9999;
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_CHAIN).getOperations().get(PUT_OPERATION_NUMBER).getAction().setMessageNumber(impossibleMessage);
        client.runOperationChain(SEQUENTIAL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(throwable -> {
            ClientException exception = (ClientException) throwable;
            Assert.assertEquals(404, exception.getStatusCode());
            Assert.assertEquals("Could not find entry for uuid 'null'", exception.getMessage());
            checkCollectorGenericMetrics(1, 1, 0, 0, 0, 1);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_CHAIN).getOperations().get(PUT_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
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
        client.runOperationChain(PARALLEL_OPERATION_CHAIN).onComplete(context.asyncAssertSuccess(v ->
            checkCollectorGenericMetrics(0, 3, 0, 0, 0, 0)));
    }

    @Test
    public void TestCoordinatorCanFailOperationChainInParallel(TestContext context) throws IOException, NullPointerException  {
        api = null;
        server = null;
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.runOperationChain(PARALLEL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(error -> {
            ClientException exception = (ClientException) error;
            Assert.assertEquals(400, exception.getStatusCode());
            Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(0, 0, 0, 0, 3, 0);

            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().get(0),
                client.getCollector().getMismatchedResponses().get(0),
                exception);

            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().get(1),
                client.getCollector().getMismatchedResponses().get(1),
                exception);

            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().get(2),
                client.getCollector().getMismatchedResponses().get(2),
                exception);
        }));
    }

    @Test
    public void TestCoordinatorFailsIfOnlyOneOperationChainFailsInParallel(TestContext context) throws IOException, NullPointerException  {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().get(1).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.runOperationChain(PARALLEL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(error -> {
            ClientException exception = (ClientException) error;
            Assert.assertEquals(500, exception.getStatusCode());
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(0, 2, 0, 0, 1, 0);

            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().get(1),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
        }));
    }

    @Test
    public void TestCoordinatorFailsIfMultipleOperationChainFailsInParallel(TestContext context) throws IOException, NullPointerException  {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().get(1).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().get(2).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.runOperationChain(PARALLEL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(error -> {
            ClientException exception = (ClientException) error;
            Assert.assertEquals(500, exception.getStatusCode());
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(0, 1, 0, 0, 2, 0);

            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().get(1),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_CHAIN).getOperations().get(2),
                client.getCollector().getMismatchedResponses().get(1),
                exception);
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
            ClientException exception = (ClientException) throwable;
            Assert.assertEquals(500, exception.getStatusCode());
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(0, 0, 0, 0, 1, 0);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_CHAIN).getOperations().get(POST_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
        }));
    }

    @Test
    public void TestCoordinatorThrowsOutOfBoundExceptionOnPUTOperation(TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_CHAIN).getOperations().get(PUT_OPERATION_NUMBER).getAction().setNewMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.runOperationChain(SEQUENTIAL_OPERATION_CHAIN).onComplete(context.asyncAssertFailure(throwable -> {
            ClientException exception = (ClientException) throwable;
            Assert.assertEquals(500, exception.getStatusCode());
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(1, 1, 0, 0, 0, 1);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_CHAIN).getOperations().get(PUT_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
        }));
    }

    private void checkMismatchedResponse(Operation operation, MismatchedResponse response, ClientException exception) {
        Assert.assertEquals(operation.getAction().getMethod(), response.getMethod());
        Assert.assertEquals(operation.getAction().getMessageNumber().intValue(), response.getMessageNumber());
        Assert.assertEquals(operation.getExpected().getStatusCode(), response.getExpectedStatusCode());
        Assert.assertEquals(exception.getStatusCode(), response.getActualStatusCode());
        Assert.assertEquals(exception.getMessage(), response.getError());
    }

    private void checkCollectorGenericMetrics(int successfulGet, int successfulPost, int successfulPut, int failedGet, int failedPost, int failedPut) {
        int totalSuccess = successfulGet + successfulPost + successfulPut;
        int totalFail = failedGet + failedPost + failedPut;

        Assert.assertEquals(totalSuccess + totalFail, client.getCollector().getGeneral().getTotal());
        Assert.assertEquals(totalFail, client.getCollector().getGeneral().getFailed());
        Assert.assertEquals(totalSuccess, client.getCollector().getGeneral().getSucceeded());
        Assert.assertEquals(totalFail, client.getCollector().getMismatchedResponses().size());

        Assert.assertEquals(successfulGet, client.getCollector().getGeneral().getSucceeded(RequestMethod.GET));
        Assert.assertEquals(successfulPost, client.getCollector().getGeneral().getSucceeded(RequestMethod.POST));
        Assert.assertEquals(successfulPut, client.getCollector().getGeneral().getSucceeded(RequestMethod.PUT));

        Assert.assertEquals(failedGet, client.getCollector().getGeneral().getFailed(RequestMethod.GET));
        Assert.assertEquals(failedPost, client.getCollector().getGeneral().getFailed(RequestMethod.POST));
        Assert.assertEquals(failedPut, client.getCollector().getGeneral().getFailed(RequestMethod.PUT));
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
