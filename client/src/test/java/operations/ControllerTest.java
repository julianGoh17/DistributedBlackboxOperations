package operations;

import io.julian.client.io.InputReader;
import io.julian.client.io.OutputPrinter;
import io.julian.client.io.TerminalInputHandler;
import io.julian.client.io.TerminalOutputHandler;
import io.julian.client.model.RequestMethod;
import io.julian.client.model.operation.Action;
import io.julian.client.model.operation.Configuration;
import io.julian.client.model.operation.Expected;
import io.julian.client.model.operation.Operation;
import io.julian.client.model.operation.OperationChain;
import io.julian.client.operations.ClientConfiguration;
import io.julian.client.operations.Controller;
import io.julian.client.operations.Coordinator;
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
import org.mockito.InOrder;

import java.io.IOException;
import java.util.Collections;

import static io.julian.client.io.TerminalOutputHandler.HEADER_CHAR;
import static io.julian.client.io.TerminalOutputHandler.MESSAGES_HEADER;
import static io.julian.client.io.TerminalOutputHandler.OPERATION_CHAIN_HEADER;
import static io.julian.client.metrics.Reporter.REPORT_FILE_NAME;
import static io.julian.client.operations.Controller.INVALID_LOOPED_MESSAGE_CONFIGURATION_MESSAGE;
import static io.julian.client.operations.Controller.INVALID_OPERATION_CHAIN_MESSAGE;
import static io.julian.client.operations.Controller.LOOPED_POST;
import static io.julian.client.operations.Controller.RUNNING_LOOPED_POST;
import static io.julian.client.operations.Controller.STATE_CHECK;
import static io.julian.client.operations.Controller.SUPPLY_MESSAGES_PER_SECOND_MESSAGE;
import static io.julian.client.operations.Controller.SUPPLY_NUMBER_OF_SECONDS_MESSAGE;
import static io.julian.client.operations.Controller.SUPPLY_VALID_OPERATION_CHAIN_MESSAGE;
import static io.julian.client.operations.Controller.TERMINATING_CLIENT_MESSAGE;
import static io.julian.client.operations.Controller.VALID_OPERATION_CHAIN_MESSAGE;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(VertxUnitRunner.class)
public class ControllerTest extends AbstractClientTest {
    private final JsonObject message = new JsonObject().put("test", "object");
    private final Operation operation = new Operation();
    private Coordinator coordinator;
    private final InputReader inputReader = mock(InputReader.class);
    private final OutputPrinter outputPrinter = mock(OutputPrinter.class);
    private TerminalInputHandler input;
    private final TerminalOutputHandler output = new TerminalOutputHandler(outputPrinter);

    private static final String VALID_OPERATION_CHAIN_NAME = "test-operation-chain";
    private static final String INVALID_OPERATION_CHAIN_NAME = "invalid-operation-chain";

    private static final String TEST_MESSAGE_FILES_PATH = String.format("%s/src/test/resources/messages", System.getProperty("user.dir"));
    private static final String TEST_OPERATION_FILES_PATH = String.format("%s/src/test/resources/operations", System.getProperty("user.dir"));
    // Putting in /tmp so that we have permissions to create file in Github Action pipeline
    private static final String TEST_REPORT_FILE_PATH = "/tmp";

    @Before
    public void before() throws IOException  {
        this.vertx = Vertx.vertx();
        this.input = new TerminalInputHandler(inputReader, vertx);
        this.coordinator = new Coordinator(vertx, new ClientConfiguration());
        this.coordinator.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
        this.coordinator.getMemory().setOriginalMessages(Collections.singletonList(message));

        Action action = new Action();
        action.setMethod(RequestMethod.GET);
        action.setMessageNumber(1);
        operation.setAction(action);

        operation.setExpected(new Expected(202));

        OperationChain operationChain = new OperationChain(Collections.singletonList(operation), new Configuration());
        this.coordinator.setOperationChains(Collections.singletonMap(VALID_OPERATION_CHAIN_NAME, operationChain));
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void TestControllerSuccessfullyCreatesReport(final TestContext context) {
        String reportFilePath = String.format("%s/%s", TEST_REPORT_FILE_PATH, REPORT_FILE_NAME);
        Controller controller = new Controller(input, output, coordinator, vertx);
        when(inputReader.nextLine())
            .thenReturn("1")
            .thenReturn(VALID_OPERATION_CHAIN_NAME)
            .thenReturn("6");

        Async async = context.async();
        controller.run(TEST_REPORT_FILE_PATH)
            .onComplete(context.asyncAssertSuccess(v -> vertx.fileSystem().exists(reportFilePath, context.asyncAssertSuccess(v2 -> {
                InOrder order = inOrder(inputReader, outputPrinter);
                testControllerPrintsOperation(order);
                order.verify(inputReader).nextLine();
                order.verify(outputPrinter).println(SUPPLY_VALID_OPERATION_CHAIN_MESSAGE);
                order.verify(outputPrinter).println(HEADER_CHAR.repeat(SUPPLY_VALID_OPERATION_CHAIN_MESSAGE.length()));
                order.verify(inputReader).nextLine();
                order.verify(outputPrinter).println(String.format(VALID_OPERATION_CHAIN_MESSAGE, VALID_OPERATION_CHAIN_NAME));

                testControllerPrintsOperation(order);
                order.verify(inputReader).nextLine();
                order.verify(outputPrinter).println(TERMINATING_CLIENT_MESSAGE);
                order.verifyNoMoreInteractions();
                vertx.fileSystem().delete(reportFilePath, context.asyncAssertSuccess());
                async.complete();
            }))));
        async.awaitSuccess();
    }

    @Test
    public void TestControllerHandlesMultipleAnswers(final TestContext context) {
        String reportFilePath = String.format("%s/%s", TEST_REPORT_FILE_PATH, REPORT_FILE_NAME);
        Controller controller = new Controller(input, output, coordinator, vertx);
        when(inputReader.nextLine())
            .thenReturn("1")
            .thenReturn(VALID_OPERATION_CHAIN_NAME)
            .thenReturn("3")
            .thenReturn("6");

        Async async = context.async();
        controller.run(TEST_REPORT_FILE_PATH)
            .onComplete(context.asyncAssertSuccess(v -> vertx.fileSystem().exists(reportFilePath, context.asyncAssertSuccess(v2 -> {
                InOrder order = inOrder(inputReader, outputPrinter);

                testControllerPrintsOperation(order);
                order.verify(inputReader).nextLine();
                order.verify(outputPrinter).println(SUPPLY_VALID_OPERATION_CHAIN_MESSAGE);
                order.verify(outputPrinter).println(HEADER_CHAR.repeat(SUPPLY_VALID_OPERATION_CHAIN_MESSAGE.length()));
                order.verify(inputReader).nextLine();
                order.verify(outputPrinter).println(String.format(VALID_OPERATION_CHAIN_MESSAGE, VALID_OPERATION_CHAIN_NAME));

                testControllerPrintsOperation(order);
                order.verify(outputPrinter).println(MESSAGES_HEADER);
                order.verify(outputPrinter).println(HEADER_CHAR.repeat(MESSAGES_HEADER.length()));
                order.verify(outputPrinter).println(String.format("Message '0': %s", message.encodePrettily()));

                testControllerPrintsOperation(order);
                order.verify(inputReader).nextLine();
                order.verify(outputPrinter).println(TERMINATING_CLIENT_MESSAGE);
                order.verifyNoMoreInteractions();

                vertx.fileSystem().delete(reportFilePath, context.asyncAssertSuccess());
                async.complete();
            }))));
        async.awaitSuccess();
    }

    @Test
    public void TestControllerFailsCreatesReport(final TestContext context) {
        String wrongReportPath = String.format("%s/fake-path", TEST_REPORT_FILE_PATH);
        String wrongReportFilePath = String.format("%s/%s", wrongReportPath, REPORT_FILE_NAME);
        Controller controller = new Controller(input, output, coordinator, vertx);
        when(inputReader.nextLine())
            .thenReturn("1")
            .thenReturn(VALID_OPERATION_CHAIN_NAME)
            .thenReturn("6");

        Async async = context.async();
        controller.run(wrongReportPath)
            .onComplete(context.asyncAssertFailure(e -> {
                Assert.assertEquals(String.format("java.nio.file.NoSuchFileException: %s", wrongReportFilePath), e.getMessage());
                InOrder order = inOrder(inputReader, outputPrinter);
                testControllerPrintsOperation(order);
                order.verify(inputReader).nextLine();
                order.verify(outputPrinter).println(SUPPLY_VALID_OPERATION_CHAIN_MESSAGE);
                order.verify(outputPrinter).println(HEADER_CHAR.repeat(SUPPLY_VALID_OPERATION_CHAIN_MESSAGE.length()));
                order.verify(inputReader).nextLine();
                order.verify(outputPrinter).println(String.format(VALID_OPERATION_CHAIN_MESSAGE, VALID_OPERATION_CHAIN_NAME));

                testControllerPrintsOperation(order);
                order.verify(inputReader).nextLine();
                order.verify(outputPrinter).println(TERMINATING_CLIENT_MESSAGE);
                order.verifyNoMoreInteractions();
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestControllerRepeatsWhenNotGivenANumber(final TestContext context) {
        Controller controller = new Controller(input, output, coordinator, vertx);
        when(inputReader.nextLine())
            .thenReturn("Not A Number")
            .thenReturn("6");
        Async async = context.async();
        controller.runOperation().onComplete(wantsToContinue -> {
            Assert.assertFalse(wantsToContinue.result());
            InOrder order = inOrder(inputReader, outputPrinter);
            testControllerPrintsOperation(order);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(Controller.NOT_GIVEN_VALID_OPERATION_ERROR_MESSAGE);

            testControllerPrintsOperation(order);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(TERMINATING_CLIENT_MESSAGE);
            order.verifyNoMoreInteractions();
            async.complete();
        });
        async.awaitSuccess();
    }

    @Test
    public void TestControllerRepeatsWhenInvalidNumberGiven(final TestContext context) {
        Controller controller = new Controller(input, output, coordinator, vertx);
        when(inputReader.nextLine())
            .thenReturn("7")
            .thenReturn("6");

        Async async = context.async();
        controller.runOperation().onComplete(wantsToContinue -> {
            Assert.assertFalse(wantsToContinue.result());
            InOrder order = inOrder(inputReader, outputPrinter);
            testControllerPrintsOperation(order);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(Controller.NOT_GIVEN_VALID_OPERATION_ERROR_MESSAGE);

            testControllerPrintsOperation(order);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(TERMINATING_CLIENT_MESSAGE);
            order.verifyNoMoreInteractions();
            async.complete();
        });
        async.awaitSuccess();
    }

    @Test
    public void TestControllerRunsOperationChain(final TestContext context) {
        Controller controller = new Controller(input, output, coordinator, vertx);
        when(inputReader.nextLine())
            .thenReturn("1")
            .thenReturn(INVALID_OPERATION_CHAIN_NAME)
            .thenReturn(VALID_OPERATION_CHAIN_NAME);

        Async async = context.async();
        controller.runOperation().onComplete(wantsToContinue -> {
            Assert.assertTrue(wantsToContinue.result());
            InOrder order = inOrder(inputReader, outputPrinter);
            testControllerPrintsOperation(order);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(SUPPLY_VALID_OPERATION_CHAIN_MESSAGE);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(String.format(INVALID_OPERATION_CHAIN_MESSAGE, INVALID_OPERATION_CHAIN_NAME));
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(String.format(VALID_OPERATION_CHAIN_MESSAGE, VALID_OPERATION_CHAIN_NAME));
            order.verifyNoMoreInteractions();
            async.complete();
        });
        async.awaitSuccess();
    }

    @Test
    public void TestControllerRunsSendCommandLineMessage(final TestContext context) {
        Controller controller = new Controller(input, output, coordinator, vertx);
        when(inputReader.nextLine())
            .thenReturn("2");

        Async async = context.async();
        controller.runOperation().onComplete(wantsToContinue -> {
            Assert.assertTrue(wantsToContinue.result());
            InOrder order = inOrder(inputReader, outputPrinter);
            testControllerPrintsOperation(order);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(STATE_CHECK);
            order.verifyNoMoreInteractions();
            async.complete();
        });
        async.awaitSuccess();
    }

    @Test
    public void TestControllerRunsPrintMessages(final TestContext context) {
        Controller controller = new Controller(input, output, coordinator, vertx);
        when(inputReader.nextLine())
            .thenReturn("3");
        Async async = context.async();
        controller.runOperation().onComplete(wantsToContinue -> {
            Assert.assertTrue(wantsToContinue.result());
            InOrder order = inOrder(inputReader, outputPrinter);
            testControllerPrintsOperation(order);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(MESSAGES_HEADER);
            order.verify(outputPrinter).println(HEADER_CHAR.repeat(MESSAGES_HEADER.length()));
            order.verify(outputPrinter).println(String.format("Message '0': %s", message.encodePrettily()));
            order.verifyNoMoreInteractions();
            async.complete();
        });
        async.awaitSuccess();
    }

    @Test
    public void TestControllerRunsPrintOperationChain(final TestContext context) {
        Controller controller = new Controller(input, output, coordinator, vertx);
        when(inputReader.nextLine())
            .thenReturn("4");
        Async async = context.async();
        controller.runOperation().onComplete(wantsToContinue -> {
            Assert.assertTrue(wantsToContinue.result());
            InOrder order = inOrder(inputReader, outputPrinter);
            testControllerPrintsOperation(order);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(OPERATION_CHAIN_HEADER);
            order.verify(outputPrinter).println(HEADER_CHAR.repeat(OPERATION_CHAIN_HEADER.length()));
            order.verify(outputPrinter).println(VALID_OPERATION_CHAIN_NAME);
            order.verifyNoMoreInteractions();
            async.complete();
        });
        async.awaitSuccess();
    }

    @Test
    public void TestControllerRunsExit(final TestContext context) {
        Controller controller = new Controller(input, output, coordinator, vertx);
        when(inputReader.nextLine())
            .thenReturn("6");

        Async async = context.async();
        controller.runOperation().onComplete(wantsToContinue -> {
            Assert.assertFalse(wantsToContinue.result());
            InOrder order = inOrder(inputReader, outputPrinter);
            testControllerPrintsOperation(order);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(TERMINATING_CLIENT_MESSAGE);
            order.verifyNoMoreInteractions();
            async.complete();
        });
        async.awaitSuccess();
    }

    private void testControllerPrintsOperation(final InOrder order) {
        order.verify(outputPrinter).println(TerminalOutputHandler.AVAILABLE_OPERATIONS);
        order.verify(outputPrinter).println("*".repeat(TerminalOutputHandler.AVAILABLE_OPERATIONS.length()));
        order.verify(outputPrinter).println("1. Run Operation Chain");
        order.verify(outputPrinter).println("2. State Check");
        order.verify(outputPrinter).println("3. Print Preconfigured Messages");
        order.verify(outputPrinter).println("4. Print Preconfigured Operation Chains");
        order.verify(outputPrinter).println("5. Looped Post");
        order.verify(outputPrinter).println("6. Exit");
    }

    @Test
    public void TestControllerRunsLoopedPostSuccessfully(final TestContext context) {
        Controller controller = new Controller(input, output, coordinator, vertx);
        when(inputReader.nextLine())
            .thenReturn("5")
            .thenReturn("1")
            .thenReturn("1");

        Async async = context.async();
        controller.runOperation().onComplete(wantsToContinue -> {
            Assert.assertTrue(wantsToContinue.result());
            InOrder order = inOrder(inputReader, outputPrinter);
            testControllerPrintsOperation(order);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(LOOPED_POST);
            order.verify(outputPrinter).println(SUPPLY_NUMBER_OF_SECONDS_MESSAGE);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(SUPPLY_MESSAGES_PER_SECOND_MESSAGE);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(String.format(RUNNING_LOOPED_POST, 1, 1));
            order.verifyNoMoreInteractions();
            async.complete();
        });
        async.awaitSuccess();
    }

    @Test
    public void TestControllerRunsLoopedPostRetries(final TestContext context) {
        Controller controller = new Controller(input, output, coordinator, vertx);
        when(inputReader.nextLine())
            .thenReturn("5")
            .thenReturn("0")
            .thenReturn("1")
            .thenReturn("1")
            .thenReturn("1");

        Async async = context.async();
        controller.runOperation().onComplete(wantsToContinue -> {
            Assert.assertTrue(wantsToContinue.result());
            InOrder order = inOrder(inputReader, outputPrinter);
            testControllerPrintsOperation(order);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(LOOPED_POST);
            order.verify(outputPrinter).println(SUPPLY_NUMBER_OF_SECONDS_MESSAGE);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(SUPPLY_MESSAGES_PER_SECOND_MESSAGE);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(INVALID_LOOPED_MESSAGE_CONFIGURATION_MESSAGE);
            order.verify(outputPrinter).println(SUPPLY_NUMBER_OF_SECONDS_MESSAGE);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(SUPPLY_MESSAGES_PER_SECOND_MESSAGE);
            order.verify(inputReader).nextLine();
            order.verify(outputPrinter).println(String.format(RUNNING_LOOPED_POST, 1, 1));
            order.verifyNoMoreInteractions();
            async.complete();
        });
        async.awaitSuccess();
    }
}
