package io;

import io.julian.client.io.OutputPrinter;
import io.julian.client.io.TerminalOutputHandler;
import io.julian.client.model.operation.Configuration;
import io.julian.client.model.operation.OperationChain;
import io.vertx.core.json.JsonObject;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Collections;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class TerminalOutputHandlerTest {
    private final OutputPrinter outputPrinter = mock(OutputPrinter.class);
    private final TerminalOutputHandler output = new TerminalOutputHandler(outputPrinter);

    @Test
    public void TestTerminalHandlerPrintsOutWhenEmptyMessages() {
        output.printMessages(Collections.emptyList());
        InOrder order = inOrder(outputPrinter);
        order.verify(outputPrinter).println(TerminalOutputHandler.MESSAGES_HEADER);
        order.verify(outputPrinter).println("*".repeat(TerminalOutputHandler.MESSAGES_HEADER.length()));
        order.verify(outputPrinter).println("No preconfigured messages");
        order.verifyNoMoreInteractions();
    }

    @Test
    public void TestTerminalHandlerPrintsOutNonEmptyMessages() {
        JsonObject message = new JsonObject().put("fried", "chicken");
        output.printMessages(Collections.singletonList(message));
        InOrder order = inOrder(outputPrinter);
        order.verify(outputPrinter).println(TerminalOutputHandler.MESSAGES_HEADER);
        order.verify(outputPrinter).println("*".repeat(TerminalOutputHandler.MESSAGES_HEADER.length()));
        order.verify(outputPrinter).println("Message '0': " + message.encodePrettily());
        order.verifyNoMoreInteractions();
    }

    @Test
    public void TestTerminalHandlerPrintsOutWhenNoOperationChains() {
        output.printOperationChains(Collections.emptyMap());
        InOrder order = inOrder(outputPrinter);
        order.verify(outputPrinter).println(TerminalOutputHandler.OPERATION_CHAIN_HEADER);
        order.verify(outputPrinter).println("*".repeat(TerminalOutputHandler.OPERATION_CHAIN_HEADER.length()));
        order.verify(outputPrinter).println("No preconfigured operation chains");
        order.verifyNoMoreInteractions();
    }

    @Test
    public void TestTerminalHandlerPrintsOutWhenNonEmptyOperationChains() {
        String key = "key";
        OperationChain chain = new OperationChain(Collections.emptyList(), new Configuration());
        output.printOperationChains(Collections.singletonMap(key, chain));
        InOrder order = inOrder(outputPrinter);
        order.verify(outputPrinter).println(TerminalOutputHandler.OPERATION_CHAIN_HEADER);
        order.verify(outputPrinter).println("*".repeat(TerminalOutputHandler.OPERATION_CHAIN_HEADER.length()));
        order.verify(outputPrinter).println(key);
        order.verifyNoMoreInteractions();
    }

    @Test
    public void TestTerminalHandlerPrintsOutAvailableOptions() {
        output.printOperations();
        InOrder order = inOrder(outputPrinter);
        order.verify(outputPrinter).println(TerminalOutputHandler.AVAILABLE_OPERATIONS);
        order.verify(outputPrinter).println("*".repeat(TerminalOutputHandler.AVAILABLE_OPERATIONS.length()));
        order.verify(outputPrinter).println("1. Run Operation Chain");
        order.verify(outputPrinter).println("2. State Check");
        order.verify(outputPrinter).println("3. Print Preconfigured Messages");
        order.verify(outputPrinter).println("4. Print Preconfigured Operation Chains");
        order.verify(outputPrinter).println("5. Looped Post");
        order.verify(outputPrinter).println("6. Exit");
        order.verifyNoMoreInteractions();
    }
}
