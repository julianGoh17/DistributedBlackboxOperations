package operations;

import io.julian.client.exception.ClientException;
import io.julian.client.model.response.MismatchedResponse;
import io.julian.client.model.RequestMethod;
import io.julian.client.model.operation.Operation;
import io.julian.client.operations.Coordinator;
import io.julian.server.components.Configuration;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(VertxUnitRunner.class)
public class CoordinatorTest extends AbstractClientTest {
    Coordinator client;
    private static final String TEST_MESSAGE_FILES_PATH = String.format("%s/src/test/resources/messages", System.getProperty("user.dir"));
    private static final String TEST_OPERATION_FILES_PATH = String.format("%s/src/test/resources/operations", System.getProperty("user.dir"));

    private static final String SEQUENTIAL_OPERATION_FILE_NAME = "sequential-test-example";
    private static final String PARALLEL_OPERATION_FILE_NAME = "parallel-test-example";
    private static final int OUT_OF_BOUND_MESSAGE_INDEX = 9999;

    private static final int POST_OPERATION_NUMBER = 0;
    private static final int GET_OPERATION_NUMBER = 1;

    private static final String CONNECTION_REFUSED_EXCEPTION = String.format("Connection refused: %s/127.0.0.1:%d", Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT);
    private static final String OUT_OF_BOUND_EXCEPTION = String.format("No original message with index '%d'", OUT_OF_BOUND_MESSAGE_INDEX);

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @After
    public void tearDown() {
        client.close();
        vertx.close();
    }

    @Test
    public void TestCoordinatorCanInitialize(final TestContext context) throws Exception {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        Assert.assertEquals(3, client.getMemory().getOriginalMessages().size());
        Assert.assertEquals(2, client.getOperationChains().size());
        Assert.assertNotNull(client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME));
        Assert.assertNotNull(client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME));
        Assert.assertNotNull(client.getClient());
    }

    /**
     * HTTP METHODS
     */
    @Test
    public void TestCoordinatorCanPOSTSuccessfully(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.sendPOST(0)
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertNull(res);
                tearDownAPIServer(context);
            }));
    }

    @Test
    public void TestCoordinatorPOSTFail(final TestContext context) throws IOException, NullPointerException {
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.sendPOST(0).onComplete(context.asyncAssertFailure(throwable ->
            Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, throwable.getMessage())));
    }

    @Test
    public void TestCoordinatorGETSuccessfully(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        int messageNum = 0;
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.sendPOST(messageNum)
            .compose(v -> {
                Assert.assertEquals(1, client.getMemory().getExpectedMapping().size());
                Assert.assertNotNull(client.getMemory().getExpectedMapping().get(messageNum));
                return client.sendGET(messageNum);
            })
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertNotNull(res);
                tearDownAPIServer(context);
            }));
    }

    @Test
    public void TestCoordinator404OnGET(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        int messageNum = 0;
        String id = "random-id";
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getMemory().associateNumberWithID(messageNum, id);
        client.sendGET(messageNum)
            .onComplete(context.asyncAssertFailure(throwable -> {
                context.assertEquals(String.format("Could not find entry for uuid '%s'", id), throwable.getMessage());
                tearDownAPIServer(context);
            }));
    }

    /**
     * SEQUENTIAL OPERATION TESTS
     */
    @Test
    public void TestCoordinatorCanRunOperationChain(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.runOperationChain(SEQUENTIAL_OPERATION_FILE_NAME).onComplete(context.asyncAssertSuccess(v -> {
            checkCollectorGenericMetrics(1, 1, 0, 0, 0, 0);
            tearDownAPIServer(context);
        }));
    }

    @Test
    public void TestCoordinatorFailsOnPOSTOperation(final TestContext context) throws IOException, NullPointerException {
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.runOperationChain(SEQUENTIAL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(throwable -> {
            ClientException exception = (ClientException) throwable;
            Assert.assertEquals(400, exception.getStatusCode());
            Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(0, 0, 0, 0, 1, 0);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(POST_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
        }));
    }

    @Test
    public void TestCoordinatorFailsOnGETOperation(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(GET_OPERATION_NUMBER).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.runOperationChain(SEQUENTIAL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(throwable -> {
            ClientException exception = (ClientException) throwable;
            Assert.assertEquals(404, exception.getStatusCode());
            Assert.assertEquals("Could not find entry for uuid 'null'", exception.getMessage());
            checkCollectorGenericMetrics(0, 1, 0, 1, 0, 0);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(GET_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
            tearDownAPIServer(context);
        }));
    }

    /**
     * PARALLEL_TESTS
     */
    @Test
    public void TestCoordinatorCanRunOperationChainInParallel(final TestContext context) throws IOException, NullPointerException  {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.runOperationChain(PARALLEL_OPERATION_FILE_NAME).onComplete(context.asyncAssertSuccess(v -> {
            checkCollectorGenericMetrics(0, 3, 0, 0, 0, 0);
            tearDownAPIServer(context);
        }));
    }

    @Test
    public void TestCoordinatorCanFailOperationChainInParallel(final TestContext context) throws IOException, NullPointerException  {
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.runOperationChain(PARALLEL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(error -> {
            ClientException exception = (ClientException) error;
            Assert.assertEquals(400, exception.getStatusCode());
            Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(0, 0, 0, 0, 3, 0);

            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(0),
                client.getCollector().getMismatchedResponses().get(0),
                exception);

            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(1),
                client.getCollector().getMismatchedResponses().get(1),
                exception);

            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(2),
                client.getCollector().getMismatchedResponses().get(2),
                exception);
        }));
    }

    @Test
    public void TestCoordinatorFailsIfOnlyOneOperationChainFailsInParallel(final TestContext context) throws IOException, NullPointerException  {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(1).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.runOperationChain(PARALLEL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(error -> {
            ClientException exception = (ClientException) error;
            Assert.assertEquals(500, exception.getStatusCode());
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(0, 2, 0, 0, 1, 0);

            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(1),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
            tearDownAPIServer(context);
        }));
    }

    @Test
    public void TestCoordinatorFailsIfMultipleOperationChainFailsInParallel(final TestContext context) throws IOException, NullPointerException  {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(1).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(2).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.runOperationChain(PARALLEL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(error -> {
            ClientException exception = (ClientException) error;
            Assert.assertEquals(500, exception.getStatusCode());
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(0, 1, 0, 0, 2, 0);

            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(1),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(2),
                client.getCollector().getMismatchedResponses().get(1),
                exception);
            tearDownAPIServer(context);
        }));
    }

    /**
     * EXCEPTION TESTS
     */
    @Test
    public void TestCoordinatorThrowsOutOfBoundExceptionOnPOSTOperation(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(POST_OPERATION_NUMBER).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.runOperationChain(SEQUENTIAL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(throwable -> {
            ClientException exception = (ClientException) throwable;
            Assert.assertEquals(500, exception.getStatusCode());
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(0, 0, 0, 0, 1, 0);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(POST_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
            tearDownAPIServer(context);
        }));
    }

    private void checkMismatchedResponse(final Operation operation, final MismatchedResponse response, final ClientException exception) {
        Assert.assertEquals(operation.getAction().getMethod(), response.getMethod());
        Assert.assertEquals(operation.getAction().getMessageNumber().intValue(), response.getMessageNumber());
        Assert.assertEquals(operation.getExpected().getStatusCode(), response.getExpectedStatusCode());
        Assert.assertEquals(exception.getStatusCode(), response.getActualStatusCode());
        Assert.assertEquals(exception.getMessage(), response.getError());
    }

    private void checkCollectorGenericMetrics(final int successfulGet, final int successfulPost, final int successfulPut, final int failedGet, final int failedPost, final int failedPut) {
        int totalSuccess = successfulGet + successfulPost + successfulPut;
        int totalFail = failedGet + failedPost + failedPut;

        Assert.assertEquals(totalSuccess + totalFail, client.getCollector().getGeneral().getTotal());
        Assert.assertEquals(totalFail, client.getCollector().getGeneral().getFailed());
        Assert.assertEquals(totalSuccess, client.getCollector().getGeneral().getSucceeded());
        Assert.assertEquals(totalFail, client.getCollector().getMismatchedResponses().size());

        Assert.assertEquals(successfulGet, client.getCollector().getGeneral().getSucceeded(RequestMethod.GET));
        Assert.assertEquals(successfulPost, client.getCollector().getGeneral().getSucceeded(RequestMethod.POST));

        Assert.assertEquals(failedGet, client.getCollector().getGeneral().getFailed(RequestMethod.GET));
        Assert.assertEquals(failedPost, client.getCollector().getGeneral().getFailed(RequestMethod.POST));
    }

    protected void setUpApiServer(final TestContext context) {
        super.setUpApiServer(context);
        client = new Coordinator(vertx);
    }
}
