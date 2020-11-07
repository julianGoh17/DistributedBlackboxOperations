package io.julian.client.operations;

import io.julian.client.io.TerminalInputHandler;
import io.julian.client.io.TerminalOutputHandler;
import io.julian.client.metrics.Reporter;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private final Vertx vertx;
    private final Reporter reporter;

    public final static String NOT_GIVEN_VALID_OPERATION_ERROR_MESSAGE =  "Please give a valid operation number";
    public final static String SUPPLY_VALID_OPERATION_CHAIN_MESSAGE = "Please input the name of the operation chain to run";
    public final static String INVALID_OPERATION_CHAIN_MESSAGE = "Invalid operation chain name '%s', please supply a valid one";
    public final static String VALID_OPERATION_CHAIN_MESSAGE = "Running operation chain '%s'";
    public final static String SENDING_COMMAND_LINE_MESSAGE = "Sending Command Line Message is currently disabled";
    public final static String TERMINATING_CLIENT_MESSAGE = "Terminating client and generating report";

    public Controller(final TerminalInputHandler input, final TerminalOutputHandler output, final Coordinator coordinator, final Vertx vertx) {
        this.input = input;
        this.output = output;
        this.client = coordinator;
        this.vertx = vertx;
        this.reporter = new Reporter();
    }

    public Future<Void> run(final String reportFileLocation) {
        log.traceEntry(() -> reportFileLocation);
        final AtomicBoolean inFlightCommand = new AtomicBoolean(false);
        final Promise<Void> createdReport = Promise.promise();
        vertx.setPeriodic(1000, id -> {
            if (!inFlightCommand.get()) {
                inFlightCommand.set(true);
                runOperation()
                    .onComplete(userWantsToContinue -> {
                        if (userWantsToContinue.result()) {
                            inFlightCommand.set(false);
                        } else {
                            reporter.createReportFile(client.getCollector().getMismatchedResponses(), client.getCollector().getGeneral(), reportFileLocation, vertx)
                                .onSuccess(v -> createdReport.complete())
                                .onFailure(createdReport::fail);
                            vertx.cancelTimer(id);
                        }
                    });
            }
        });
        return log.traceExit(createdReport.future());
    }

    public Future<Boolean> runOperation() {
        log.traceEntry();
        boolean hasNotGivenValidOperation = true;
        Promise<Boolean> userWantsToContinue = Promise.promise();
        while (hasNotGivenValidOperation) {
            output.printOperations();
            try {
                hasNotGivenValidOperation = false;
                switch (input.getNumberFromInput()) {
                    case OPERATION_CHAIN_NUMBER:
                        runOperationChain()
                            .onComplete(v -> userWantsToContinue.complete(true));
                        break;
                    // TODO: Add ability to send command line message
                    case SEND_COMMAND_LINE_MESSAGE_NUMBER:
                        runSendCommandLineMessage();
                        userWantsToContinue.complete(true);
                        break;
                    case PRINT_MESSAGES_NUMBER:
                        runPreconfiguredMessages();
                        userWantsToContinue.complete(true);
                        break;
                    case PRINT_OPERATION_CHAIN_NUMBER:
                        runOperationChains();
                        userWantsToContinue.complete(true);
                        break;
                    case EXIT_NUMBER:
                        runExit();
                        userWantsToContinue.complete(false);
                        break;
                    default:
                        output.println(NOT_GIVEN_VALID_OPERATION_ERROR_MESSAGE);
                        hasNotGivenValidOperation = true;
                        break;
                }
            } catch (NumberFormatException e) {
                log.error(e);
                output.println(NOT_GIVEN_VALID_OPERATION_ERROR_MESSAGE);
                hasNotGivenValidOperation = true;
            }
        }
        return userWantsToContinue.future();
    }

    private Future<Void> runOperationChain() {
        log.traceEntry();
        output.printHeader(SUPPLY_VALID_OPERATION_CHAIN_MESSAGE);
        boolean hasNotSuppliedValidOperationChainName = true;
        String suppliedName = "";
        while (hasNotSuppliedValidOperationChainName) {
            suppliedName = input.getStringFromInput();
            hasNotSuppliedValidOperationChainName = !client.getOperationChains().containsKey(suppliedName);
            if (hasNotSuppliedValidOperationChainName) {
                output.println(String.format(INVALID_OPERATION_CHAIN_MESSAGE, suppliedName));
            }
        }
        output.println(String.format(VALID_OPERATION_CHAIN_MESSAGE, suppliedName));
        return log.traceExit(client.runOperationChain(suppliedName));
    }

    private void runSendCommandLineMessage() {
        log.traceEntry();
        output.println(SENDING_COMMAND_LINE_MESSAGE);
        log.traceExit();
    }

    private void runPreconfiguredMessages() {
        log.traceEntry();
        output.printMessages(client.getMemory().getOriginalMessages());
        log.traceExit();
    }

    private void runOperationChains() {
        log.traceEntry();
        output.printOperationChains(client.getOperationChains());
        log.traceExit();
    }

    private void runExit() {
        log.traceEntry();
        output.println(TERMINATING_CLIENT_MESSAGE);
        log.traceExit();
    }
}
