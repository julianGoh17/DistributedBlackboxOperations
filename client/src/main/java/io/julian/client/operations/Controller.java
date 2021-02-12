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
import java.util.concurrent.atomic.AtomicReference;

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
        log.info(String.format("Starting %s", Controller.class.getSimpleName()));
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
        AtomicBoolean hasNotGivenValidOperation = new AtomicBoolean(true);
        Promise<Boolean> userWantsToContinue = Promise.promise();
        log.info("Running Operation");
        vertx.setPeriodic(1000, id -> {
            while (hasNotGivenValidOperation.get()) {
                log.info("Waiting for user to give operation");
                hasNotGivenValidOperation.set(false);
                output.printOperations();
                input.getNumberFromInput().onSuccess(num -> {
                    log.debug(String.format("Retrieved '%d' from commandline", num));
                    switch (num) {
                        case OPERATION_CHAIN_NUMBER:
                            runOperationChain()
                                .onComplete(v -> {
                                    userWantsToContinue.complete(true);
                                    vertx.cancelTimer(id);
                                });
                            break;
                        // TODO: Add ability to send command line message
                        case SEND_COMMAND_LINE_MESSAGE_NUMBER:
                            log.info("Sending command line message");
                            runSendCommandLineMessage();
                            userWantsToContinue.complete(true);
                            vertx.cancelTimer(id);
                            break;
                        case PRINT_MESSAGES_NUMBER:
                            log.info("Printing messages");
                            printPreconfiguredMessages();
                            userWantsToContinue.complete(true);
                            vertx.cancelTimer(id);
                            break;
                        case PRINT_OPERATION_CHAIN_NUMBER:
                            log.info("Printing operation chains");
                            printOperationChains();
                            userWantsToContinue.complete(true);
                            vertx.cancelTimer(id);
                            break;
                        case EXIT_NUMBER:
                            log.info("Exiting client");
                            runExit();
                            userWantsToContinue.complete(false);
                            vertx.cancelTimer(id);
                            break;
                        default:
                            output.println(NOT_GIVEN_VALID_OPERATION_ERROR_MESSAGE);
                            hasNotGivenValidOperation.set(true);
                            break;
                    }
                }).onFailure(throwable -> {
                    log.error(throwable.getMessage());
                    output.println(NOT_GIVEN_VALID_OPERATION_ERROR_MESSAGE);
                    hasNotGivenValidOperation.set(true);
                });
            }
        });
        return userWantsToContinue.future();
    }

    public void close() {
        log.traceEntry();
        client.close();
        log.traceExit();
    }

    private Future<Void> runOperationChain() {
        log.traceEntry();
        output.printHeader(SUPPLY_VALID_OPERATION_CHAIN_MESSAGE);
        AtomicBoolean inFlight = new AtomicBoolean();
        AtomicReference<String> suppliedName = new AtomicReference<>();
        Promise<Void> operationRes = Promise.promise();
        log.info("Running Operation Chain");
        vertx.setPeriodic(1000, id -> {
            if (suppliedName.get() == null & !inFlight.get()) {
                inFlight.set(true);
                input.getStringFromInput().onComplete(name -> {
                    if (client.getOperationChains().containsKey(name.result())) {
                        log.info(String.format("Running operation chain '%s'", name.result()));
                        suppliedName.set(name.result());
                    } else {
                        log.error(String.format("Could not find registered operation chain '%s'", name.result()));
                    }
                    inFlight.set(false);
                });
            } else if (suppliedName.get() != null) {
                vertx.cancelTimer(id);
                output.println(String.format(VALID_OPERATION_CHAIN_MESSAGE, suppliedName));
                client.runOperationChain(suppliedName.get())
                    .onSuccess(v -> operationRes.complete())
                    .onFailure(operationRes::fail);
            }
        });
        return log.traceExit(operationRes.future());
    }

    private void runSendCommandLineMessage() {
        log.traceEntry();
        output.println(SENDING_COMMAND_LINE_MESSAGE);
        log.traceExit();
    }

    private void printPreconfiguredMessages() {
        log.traceEntry();
        output.printMessages(client.getMemory().getOriginalMessages());
        log.traceExit();
    }

    private void printOperationChains() {
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
