package operations;

import io.julian.client.exception.ClientException;
import io.julian.client.model.RequestMethod;
import io.julian.client.model.operation.Expected;
import io.julian.client.model.operation.Operation;
import io.julian.client.model.response.MismatchedResponse;
import io.julian.client.operations.Coordinator;
import io.julian.server.components.Configuration;
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
    private static final int DELETE_OPERATION_NUMBER = 2;

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
        tearDownAPIServer(context);
    }

    /**
     * HTTP METHODS
     */
    @Test
    public void TestCoordinatorCanPOSTSuccessfully(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        Async async = context.async();
        client.sendPOST(0, new Expected(200))
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertNull(res);
                async.complete();
            }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestCoordinatorPOSTFail(final TestContext context) throws IOException, NullPointerException {
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        Expected expected = new Expected(200);
        Async async = context.async();
        client.sendPOST(0, expected)
            .onComplete(context.asyncAssertFailure(throwable -> {
                Assert.assertEquals(expected.generateMismatchedException(500, CONNECTION_REFUSED_EXCEPTION).getMessage(), throwable.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestCoordinatorGETSuccessfully(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        int messageNum = 0;
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        Async async = context.async();
        client.sendPOST(0, new Expected(200))
            .compose(v -> {
                Assert.assertEquals(1, client.getMemory().getExpectedMapping().size());
                Assert.assertNotNull(client.getMemory().getExpectedMapping().get(messageNum));
                return client.sendGET(messageNum, new Expected(200));
            })
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertNotNull(res);
                async.complete();
            }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestCoordinator404OnGET(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        int messageNum = 0;
        String id = "random-id";
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getMemory().associateNumberWithID(messageNum, id);
        Expected expected = new Expected(200);
        Async async = context.async();
        client.sendGET(messageNum, expected)
            .onComplete(context.asyncAssertFailure(throwable -> {
                context.assertEquals(expected.generateMismatchedException(404, String.format("Could not find entry for uuid '%s'", id)).getMessage(),
                    throwable.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    /**
     * SEQUENTIAL OPERATION TESTS
     */
    @Test
    public void TestCoordinatorCanRunOperationChain(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        Async async = context.async();
        client.runOperationChain(SEQUENTIAL_OPERATION_FILE_NAME).onComplete(context.asyncAssertSuccess(v -> {
            checkCollectorGenericMetrics(1, 1, 1, 0, 0, 0);
            async.complete();
        }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestCoordinatorFailsOnPOSTOperation(final TestContext context) throws IOException, NullPointerException {
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        Async async = context.async();
        client.runOperationChain(SEQUENTIAL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(throwable -> {
            ClientException exception = (ClientException) throwable;
            Assert.assertEquals(500, exception.getStatusCode());
            Assert.assertEquals(new Expected(200).generateMismatchedException(500, CONNECTION_REFUSED_EXCEPTION).getMessage(), exception.getMessage());
            checkCollectorGenericMetrics(0, 0, 0, 0, 1, 0);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(POST_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
            async.complete();
        }));
        async.awaitSuccess();
    }

    @Test
    public void TestCoordinatorFailsOnGETOperation(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(GET_OPERATION_NUMBER).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        Async async = context.async();
        client.runOperationChain(SEQUENTIAL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(throwable -> {
            ClientException exception = (ClientException) throwable;
            Assert.assertEquals(404, exception.getStatusCode());
            Expected expected = new Expected(200);
            Assert.assertEquals(expected.generateMismatchedException(404, "Could not find entry for uuid 'null'").getMessage(), exception.getMessage());
            checkCollectorGenericMetrics(0, 1, 0, 1, 0, 0);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(GET_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
            async.complete();
        }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestCoordinatorFailsOnDELETEOperation(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(DELETE_OPERATION_NUMBER).getAction().setMessageNumber(2);
        Async async = context.async();
        client.runOperationChain(SEQUENTIAL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(throwable -> {
            ClientException exception = (ClientException) throwable;
            Expected expected = new Expected(200);
            Assert.assertEquals(404, exception.getStatusCode());
            Assert.assertEquals(
                expected.generateMismatchedException(404, "Couldn't delete message with uuid 'null' from server").getMessage(),
                exception.getMessage());
            checkCollectorGenericMetrics(1, 1, 0, 0, 0, 1);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(DELETE_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
            async.complete();
        }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestCoordinatorPassesOperationChain(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(DELETE_OPERATION_NUMBER).getAction().setMessageNumber(1);
        Async async = context.async();
        client.runOperationChain(SEQUENTIAL_OPERATION_FILE_NAME)
            .onComplete(context.asyncAssertSuccess(res -> {
                checkCollectorGenericMetrics(1, 1, 1, 0, 0, 0);
                async.complete();
            }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestCoordinatorFailsOperationChainIfMismatchedStatusCode(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(DELETE_OPERATION_NUMBER).getExpected().setStatusCode(1);
        Async async = context.async();
        client.runOperationChain(SEQUENTIAL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(throwable -> {
            ClientException exception = (ClientException) throwable;
            Assert.assertEquals(200, exception.getStatusCode());
            Assert.assertEquals(String.format(Expected.MISMATCHED_STATUS_CODE_ERROR_FORMAT, 200, 1) + Expected.CLIENT_ERROR, exception.getMessage());
            checkCollectorGenericMetrics(1, 1, 0, 0, 0, 1);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(DELETE_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
            async.complete();
        }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestCoordinatorPassesOperationChainIfStatusCodeMatches(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        int nonExistentID = 1;
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(DELETE_OPERATION_NUMBER).getAction().setMessageNumber(nonExistentID);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(DELETE_OPERATION_NUMBER).getExpected().setStatusCode(404);
        client.getMemory().getOriginalMessages().add(new JsonObject());
        client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().remove(1);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().remove(0);
        Async async = context.async();
        client.runOperationChain(SEQUENTIAL_OPERATION_FILE_NAME).onComplete(context.asyncAssertSuccess(v -> {
            checkCollectorGenericMetrics(0, 0, 1, 0, 0, 0);
            async.complete();
        }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    /**
     * PARALLEL_TESTS
     */
    @Test
    public void TestCoordinatorCanRunOperationChainInParallel(final TestContext context) throws IOException, NullPointerException  {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        Async async = context.async();
        client.runOperationChain(PARALLEL_OPERATION_FILE_NAME).onComplete(context.asyncAssertSuccess(v -> {
            checkCollectorGenericMetrics(0, 3, 0, 0, 0, 0);
            async.complete();
        }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestCoordinatorCanFailOperationChainInParallel(final TestContext context) throws IOException, NullPointerException  {
        client = new Coordinator(vertx);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        Async async = context.async();
        client.runOperationChain(PARALLEL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(error -> {
            ClientException exception = (ClientException) error;
            Assert.assertEquals(500, exception.getStatusCode());
            Assert.assertEquals(new Expected(200).generateMismatchedException(500, CONNECTION_REFUSED_EXCEPTION).getMessage(), exception.getMessage());
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
            async.complete();
        }));
        async.awaitSuccess();
    }

    @Test
    public void TestCoordinatorFailsIfOnlyOneOperationChainFailsInParallel(final TestContext context) throws IOException, NullPointerException  {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(1).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        Async async = context.async();
        client.runOperationChain(PARALLEL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(error -> {
            ClientException exception = (ClientException) error;
            Assert.assertEquals(500, exception.getStatusCode());
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(0, 2, 0, 0, 1, 0);

            checkMismatchedResponse(client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(1),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
            async.complete();
        }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestCoordinatorFailsIfMultipleOperationChainFailsInParallel(final TestContext context) throws IOException, NullPointerException  {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(1).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        client.getOperationChains().get(PARALLEL_OPERATION_FILE_NAME).getOperations().get(2).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        Async async = context.async();
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
            async.complete();
        }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    @Test
    public void TestCoordinatorChecksStateSuccessfully(final TestContext context) throws IOException, NullPointerException  {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        Async async = context.async();
        client.runOperationChain(PARALLEL_OPERATION_FILE_NAME)
            .compose(v -> client.checkState())
            .onComplete(context.asyncAssertSuccess(v -> {
                checkCollectorGenericMetrics(0, 3, 0, 0, 0, 0);
                Assert.assertEquals(1, client.getCollector().getOverviewComparisons().size());
                async.complete();
            }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    /**
     * EXCEPTION TESTS
     */
    @Test
    public void TestCoordinatorThrowsOutOfBoundExceptionOnPOSTOperation(final TestContext context) throws IOException, NullPointerException {
        setUpApiServer(context);
        client.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(POST_OPERATION_NUMBER).getAction().setMessageNumber(OUT_OF_BOUND_MESSAGE_INDEX);
        Async async = context.async();
        client.runOperationChain(SEQUENTIAL_OPERATION_FILE_NAME).onComplete(context.asyncAssertFailure(throwable -> {
            ClientException exception = (ClientException) throwable;
            Assert.assertEquals(500, exception.getStatusCode());
            Assert.assertEquals(OUT_OF_BOUND_EXCEPTION, exception.getMessage());
            checkCollectorGenericMetrics(0, 0, 0, 0, 1, 0);

            checkMismatchedResponse(client.getOperationChains().get(SEQUENTIAL_OPERATION_FILE_NAME).getOperations().get(POST_OPERATION_NUMBER),
                client.getCollector().getMismatchedResponses().get(0),
                exception);
            async.complete();
        }));
        async.awaitSuccess();
        tearDownAPIServer(context);
    }

    private void checkMismatchedResponse(final Operation operation, final MismatchedResponse response, final ClientException exception) {
        Assert.assertEquals(operation.getAction().getMethod(), response.getMethod());
        Assert.assertEquals(operation.getAction().getMessageNumber().intValue(), response.getMessageNumber());
        Assert.assertEquals(operation.getExpected().getStatusCode(), response.getExpectedStatusCode());
        Assert.assertEquals(exception.getStatusCode(), response.getActualStatusCode());
        Assert.assertEquals(exception.getMessage(), response.getError());
    }

    private void checkCollectorGenericMetrics(final int successfulGet, final int successfulPost, final int successfulDelete, final int failedGet, final int failedPost, final int failedDelete) {
        int totalSuccess = successfulGet + successfulPost + successfulDelete;
        int totalFail = failedGet + failedPost + failedDelete;

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
