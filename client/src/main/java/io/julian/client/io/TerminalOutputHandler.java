package io.julian.client.io;

import io.julian.client.model.operation.OperationChain;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class TerminalOutputHandler {
    private final static Logger log = LogManager.getLogger(TerminalOutputHandler.class.getName());
    private final OutputPrinter printer;

    public final static String HEADER_CHAR = "*";

    public final static String OPERATION_CHAIN_HEADER = "Preconfigured Operation Chains";
    public final static String MESSAGES_HEADER = "Preconfigured Messages";
    public final static String AVAILABLE_OPERATIONS = "Available Operations";

    public final static int OPERATION_CHAIN_NUMBER = 1;
    public final static int STATE_CHECK_NUMBER = 2;
    public final static int PRINT_MESSAGES_NUMBER = 3;
    public final static int PRINT_OPERATION_CHAIN_NUMBER = 4;
    public final static int EXIT_NUMBER = 5;

    public TerminalOutputHandler(final OutputPrinter printer) {
        this.printer = printer;
    }

    public void printOperations() {
        log.traceEntry();
        printHeader(AVAILABLE_OPERATIONS);
        printer.println(String.format("%d. Run Operation Chain", OPERATION_CHAIN_NUMBER));
        printer.println(String.format("%d. State Check", STATE_CHECK_NUMBER));
        printer.println(String.format("%d. Print Preconfigured Messages", PRINT_MESSAGES_NUMBER));
        printer.println(String.format("%d. Print Preconfigured Operation Chains", PRINT_OPERATION_CHAIN_NUMBER));
        printer.println(String.format("%d. Exit", EXIT_NUMBER));
        log.traceExit();
    }

    public void printOperationChains(final Map<String, OperationChain> operationChain) {
        log.traceEntry(() -> operationChain);
        printHeader(OPERATION_CHAIN_HEADER);
        if (operationChain.isEmpty()) {
            printer.println("No preconfigured operation chains");
        } else {
            operationChain.keySet().forEach(printer::println);
        }
        log.traceExit();
    }

    public void printMessages(final List<JsonObject> messages) {
        log.traceEntry(() -> messages);
        printHeader(MESSAGES_HEADER);
        if (messages.size() == 0) {
            printer.println("No preconfigured messages");
        } else {
            for (int i = 0; i < messages.size(); i++) {
                printer.println(String.format("Message '%d': %s", i, messages.get(i).encodePrettily()));
            }
        }
        log.traceExit();
    }

    public void printHeader(final String header) {
        log.traceEntry(() -> header);
        printer.println(header);
        printer.println(HEADER_CHAR.repeat(header.length()));
        log.traceExit();
    }

    public void println(final String line) {
        log.traceEntry(line);
        printer.println(line);
        log.traceExit();
    }
}
