package operations;

import io.julian.client.io.InputReader;
import io.julian.client.io.OutputPrinter;
import io.julian.client.io.TerminalInputHandler;
import io.julian.client.io.TerminalOutputHandler;
import io.julian.client.operations.Controller;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import java.io.IOException;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(VertxUnitRunner.class)
public class ControllerTest extends AbstractClientTest {
    private final InputReader inputReader = mock(InputReader.class);
    private final OutputPrinter outputPrinter = mock(OutputPrinter.class);
    private final TerminalInputHandler input = new TerminalInputHandler(inputReader);
    private final TerminalOutputHandler output = new TerminalOutputHandler(outputPrinter);

    private static final String TEST_MESSAGE_FILES_PATH = String.format("%s/src/test/resources/messages", System.getProperty("user.dir"));
    private static final String TEST_OPERATION_FILES_PATH = String.format("%s/src/test/resources/operations", System.getProperty("user.dir"));

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void TestControllerFailsInitialize() {
        try {
            Controller controller = new Controller(input, output, vertx);
            controller.initialize(String.format("%s/fake-path-1234", TEST_MESSAGE_FILES_PATH), String.format("%s/fake-path-1234", TEST_OPERATION_FILES_PATH));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(NullPointerException.class, e.getClass());
        }
    }

    @Test
    public void TestControllerSuccessfullyInitialize() throws NullPointerException, IOException {
        Controller controller = new Controller(input, output, vertx);
        controller.initialize(TEST_MESSAGE_FILES_PATH, TEST_OPERATION_FILES_PATH);
    }

    @Test
    public void TestControllerRepeatsWhenNotGivenANumber() {
        Controller controller = new Controller(input, output, vertx);
        when(inputReader.nextLine())
            .thenReturn("Not A Number")
            .thenReturn("5");
        controller.runOperation();

        InOrder order = inOrder(inputReader, outputPrinter);
        testControllerPrintsOperation(order);
        order.verify(inputReader).nextLine();
        order.verify(outputPrinter).println(Controller.NOT_GIVEN_VALID_OPERATION_ERROR);

        testControllerPrintsOperation(order);
        order.verify(inputReader).nextLine();
        order.verify(outputPrinter).println("Exit");
        order.verifyNoMoreInteractions();
    }

    @Test
    public void TestControllerRepeatsWhenInvalidNumberGiven() {
        Controller controller = new Controller(input, output, vertx);
        when(inputReader.nextLine())
            .thenReturn("7")
            .thenReturn("5");
        controller.runOperation();

        InOrder order = inOrder(inputReader, outputPrinter);
        testControllerPrintsOperation(order);
        order.verify(inputReader).nextLine();
        order.verify(outputPrinter).println(Controller.NOT_GIVEN_VALID_OPERATION_ERROR);

        testControllerPrintsOperation(order);
        order.verify(inputReader).nextLine();
        order.verify(outputPrinter).println("Exit");
        order.verifyNoMoreInteractions();
    }

    @Test
    public void TestControllerRunsOperationChain() {
        Controller controller = new Controller(input, output, vertx);
        when(inputReader.nextLine())
            .thenReturn("1");
        controller.runOperation();

        InOrder order = inOrder(inputReader, outputPrinter);
        testControllerPrintsOperation(order);
        order.verify(inputReader).nextLine();
        order.verify(outputPrinter).println("Operation Chain");
        order.verifyNoMoreInteractions();
    }

    @Test
    public void TestControllerRunsSendCommandLineMessage() {
        Controller controller = new Controller(input, output, vertx);
        when(inputReader.nextLine())
            .thenReturn("2");
        controller.runOperation();

        InOrder order = inOrder(inputReader, outputPrinter);
        testControllerPrintsOperation(order);
        order.verify(inputReader).nextLine();
        order.verify(outputPrinter).println("Send Command Line Message");
        order.verifyNoMoreInteractions();
    }

    @Test
    public void TestControllerRunsPrintMessages() {
        Controller controller = new Controller(input, output, vertx);
        when(inputReader.nextLine())
            .thenReturn("3");
        controller.runOperation();

        InOrder order = inOrder(inputReader, outputPrinter);
        testControllerPrintsOperation(order);
        order.verify(inputReader).nextLine();
        order.verify(outputPrinter).println("Print Messages");
        order.verifyNoMoreInteractions();
    }

    @Test
    public void TestControllerRunsPrintOperationChain() {
        Controller controller = new Controller(input, output, vertx);
        when(inputReader.nextLine())
            .thenReturn("4");
        controller.runOperation();

        InOrder order = inOrder(inputReader, outputPrinter);
        testControllerPrintsOperation(order);
        order.verify(inputReader).nextLine();
        order.verify(outputPrinter).println("Print Operation Chain");
        order.verifyNoMoreInteractions();
    }


    @Test
    public void TestControllerRunsExit() {
        Controller controller = new Controller(input, output, vertx);
        when(inputReader.nextLine())
            .thenReturn("5");
        controller.runOperation();

        InOrder order = inOrder(inputReader, outputPrinter);
        testControllerPrintsOperation(order);
        order.verify(inputReader).nextLine();
        order.verify(outputPrinter).println("Exit");
        order.verifyNoMoreInteractions();
    }

    private void testControllerPrintsOperation(final InOrder order) {
        order.verify(outputPrinter).println(TerminalOutputHandler.AVAILABLE_OPERATIONS);
        order.verify(outputPrinter).println("*".repeat(TerminalOutputHandler.AVAILABLE_OPERATIONS.length()));
        order.verify(outputPrinter).println("1. Run Operation Chain");
        order.verify(outputPrinter).println("2. Send Command Line Message");
        order.verify(outputPrinter).println("3. Print Preconfigured Messages");
        order.verify(outputPrinter).println("4. Print Preconfigured Operation Chains");
        order.verify(outputPrinter).println("5. Exit");
    }
}
