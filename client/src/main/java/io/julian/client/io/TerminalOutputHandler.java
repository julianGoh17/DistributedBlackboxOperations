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

    private final static String HEADER_CHAR = "*";

    public final static String OPERATION_CHAIN_HEADER = "Preconfigured Operation Chains";
    public final static String MESSAGES_HEADER = "Preconfigured Messages";
    public final static String AVAILABLE_OPERATIONS = "Available Operations";

    public final static int OPERATION_CHAIN_NUMBER = 1;
    public final static int SEND_COMMAND_LINE_MESSAGE_NUMBER = 2;
    public final static int PRINT_MESSAGES_NUMBER = 3;
    public final static int PRINT_OPERATION_CHAIN_NUMBER = 4;
    public final static int EXIT_NUMBER = 5;

    public TerminalOutputHandler(final OutputPrinter printer) {
        this.printer = printer;
    }

    public void printOperations() {
        log.traceEntry();
        printer.println(AVAILABLE_OPERATIONS);
        printer.println(getHeader(AVAILABLE_OPERATIONS));
        printer.println(String.format("%d. Run Operation Chain", OPERATION_CHAIN_NUMBER));
        printer.println(String.format("%d. Send Command Line Message", SEND_COMMAND_LINE_MESSAGE_NUMBER));
        printer.println(String.format("%d. Print Preconfigured Messages", PRINT_MESSAGES_NUMBER));
        printer.println(String.format("%d. Print Preconfigured Operation Chains", PRINT_OPERATION_CHAIN_NUMBER));
        printer.println(String.format("%d. Exit", EXIT_NUMBER));
        log.traceExit();
    }

    public void printOperationChains(final Map<String, OperationChain> operationChain) {
        log.traceEntry(() -> operationChain);
        printer.println(OPERATION_CHAIN_HEADER);
        printer.println(getHeader(OPERATION_CHAIN_HEADER));
        if (operationChain.isEmpty()) {
            printer.println("No preconfigured operation chains");
        } else {
            operationChain.keySet().forEach(printer::println);
        }

        log.traceExit();
    }

    public void printMessages(final List<JsonObject> messages) {
        log.traceEntry(() -> messages);
        printer.println(MESSAGES_HEADER);
        printer.println(getHeader(MESSAGES_HEADER));
        if (messages.size() == 0) {
            printer.println("No preconfigured messages");
        } else {
            for (int i = 0; i < messages.size(); i++) {
                printer.println(String.format("Message '%d': %s", i, messages.get(i).encodePrettily()));
            }
        }
        log.traceExit();
    }

    private String getHeader(final String header) {
        log.traceEntry(() -> header);
        return log.traceExit(HEADER_CHAR.repeat(header.length()));
    }

    public OutputPrinter getPrinter() {
        return printer;
    }
}
