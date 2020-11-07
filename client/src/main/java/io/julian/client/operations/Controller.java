package io.julian.client.operations;

import io.julian.client.io.TerminalInputHandler;
import io.julian.client.io.TerminalOutputHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.julian.client.io.TerminalOutputHandler.EXIT_NUMBER;
import static io.julian.client.io.TerminalOutputHandler.OPERATION_CHAIN_NUMBER;
import static io.julian.client.io.TerminalOutputHandler.PRINT_MESSAGES_NUMBER;
import static io.julian.client.io.TerminalOutputHandler.PRINT_OPERATION_CHAIN_NUMBER;
import static io.julian.client.io.TerminalOutputHandler.SEND_COMMAND_LINE_MESSAGE_NUMBER;

public class Controller {
    private final static Logger log = LogManager.getLogger(Controller.class.getName());

    private final TerminalInputHandler input;
    private final TerminalOutputHandler output;
    private final Coordinator client;

    public final static String NOT_GIVEN_VALID_OPERATION_ERROR =  "Please give a valid operation number";

    public Controller(final TerminalInputHandler input, final TerminalOutputHandler output, final Vertx vertx) {
        this.input = input;
        this.output = output;
        this.client = new Coordinator(vertx);
    }

    public void initialize(final String messageFilePath, final String operationChainPath) throws NullPointerException, IOException {
        log.traceEntry(() -> messageFilePath, () -> operationChainPath);
        client.initialize(messageFilePath, operationChainPath);
        log.traceExit();
    }

    public void run() {
        log.traceEntry();
        final AtomicBoolean isRunning = new AtomicBoolean(true);
        while (isRunning.get()) {
            isRunning.set(false);
        }
        log.traceExit();
    }

    public Future<Boolean> runOperation() {
        log.traceEntry();
        boolean hasNotGivenValidOperation = true;
        while (hasNotGivenValidOperation) {
            output.printOperations();
            try {
                hasNotGivenValidOperation = false;
                switch (input.getNumberFromInput()) {
                    case OPERATION_CHAIN_NUMBER:
                        output.getPrinter().println("Operation Chain");
                        break;
                    case SEND_COMMAND_LINE_MESSAGE_NUMBER:
                        output.getPrinter().println("Send Command Line Message");
                        break;
                    case PRINT_MESSAGES_NUMBER:
                        output.getPrinter().println("Print Messages");
                        break;
                    case PRINT_OPERATION_CHAIN_NUMBER:
                        output.getPrinter().println("Print Operation Chain");
                        break;
                    case EXIT_NUMBER:
                        output.getPrinter().println("Exit");
                        break;
                    default:
                        output.getPrinter().println(NOT_GIVEN_VALID_OPERATION_ERROR);
                        hasNotGivenValidOperation = true;
                        break;
                }
            } catch (NumberFormatException e) {
                log.error(e);
                output.getPrinter().println(NOT_GIVEN_VALID_OPERATION_ERROR);
                hasNotGivenValidOperation = true;
            }
        }
        return Future.succeededFuture();
    }
}
