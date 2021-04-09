package io.julian.client.operations;

import io.julian.client.exception.ClientException;
import io.julian.client.metrics.MetricsCollector;
import io.julian.client.model.RequestMethod;
import io.julian.client.model.operation.Configuration;
import io.julian.client.model.operation.Expected;
import io.julian.client.model.operation.Operation;
import io.julian.client.model.operation.OperationChain;
import io.julian.server.models.control.ServerConfiguration;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Getter
public class Coordinator {
    private static final Logger log = LogManager.getLogger(Coordinator.class.getName());
    private static final int MAX_RETRIES = 3;

    private final BaseClient client;
    private final MessageMemory memory;
    private Map<String, OperationChain> operationChains;
    private final MetricsCollector collector;
    private final Vertx vertx;
    private final ClientConfiguration configuration;

    public Coordinator(final Vertx vertx, final ClientConfiguration clientConfiguration) {
        this.vertx = vertx;
        client = new BaseClient(vertx, clientConfiguration);
        memory = new MessageMemory();
        operationChains = new HashMap<>();
        collector = new MetricsCollector();
        this.configuration = clientConfiguration;
    }

    public void initialize(final String messageFilePath, final String operationsFilePath) throws NullPointerException, IOException {
        log.traceEntry(() -> messageFilePath, () -> operationsFilePath);
        memory.readInMessageFiles(messageFilePath);
        readInOperationsFile(operationsFilePath);
        log.traceExit();
    }

    public Future<Void> checkState() {
        log.traceEntry();
        log.info("Attempting to check overview of all servers");
        Promise<Void> res = Promise.promise();
        try {
            List<ServerConfiguration> configurations = ServerFileReader.readServerFile(configuration.getServerHostsFilePath());
            List<Future> checks = configurations.stream().map(this::checkState).collect(Collectors.toList());
            CompositeFuture.all(checks)
                .onSuccess(v -> {
                    log.info("Successfully checked state of all servers");
                    res.complete();
                })
                .onFailure(cause -> {
                    log.info("Failed to check state of all servers");
                    log.error(cause.getMessage());
                    res.fail(cause);
                });
        } catch (final FileNotFoundException e) {
            log.info("Failed to check state of all servers");
            log.error(e.getMessage());
            res.fail(e);
        }
        return log.traceExit(res.future());
    }

    public Future<Void> checkState(final ServerConfiguration configuration) {
        log.traceEntry(() -> configuration);
        Promise<Void> isCheckSuccessful = Promise.promise();
        log.info(String.format("Checking overview of server '%s'", configuration.toString()));
        client.GetOverview(configuration)
            .onSuccess(res -> {
                log.info(String.format("Successfully retrieved overview of server '%s'", configuration.toString()));
                collector.addComparisonCheck(new ArrayList<>(memory.getExpectedMapping().values()), res);
                isCheckSuccessful.complete();
            })
            .onFailure(cause -> {
                log.info(String.format("Failed to retrieve overview of server '%s'", configuration.toString()));
                log.error(cause.getMessage());
                isCheckSuccessful.fail(cause.getMessage());
            });

        return log.traceExit(isCheckSuccessful.future());
    }

    public Future<Void> sendPOST(final int messageIndex, final Expected expected) throws ArrayIndexOutOfBoundsException {
        log.traceEntry(() -> messageIndex);
        Promise<Void> isPOSTSuccessful = Promise.promise();
        try {
            checkValidMessageIndex(messageIndex);
            client.POSTMessage(memory.getOriginalMessage(messageIndex), expected)
                .onSuccess(id -> {
                    log.info(String.format("Successful POST of message number '%d', received id '%s'", messageIndex, id));
                    memory.associateNumberWithID(messageIndex, id);
                    isPOSTSuccessful.complete();
                })
                .onFailure(throwable -> {
                    ClientException exception = (ClientException) throwable;
                    log.error(String.format("Unsuccessful POST of message number '%d' because: %s", messageIndex, exception.getMessage()));
                    isPOSTSuccessful.fail(throwable);
                });
        } catch (ArrayIndexOutOfBoundsException e) {
            ClientException exception = new ClientException(e.getMessage(), 500);
            log.error(exception);
            isPOSTSuccessful.fail(exception);
        }
        return log.traceExit(isPOSTSuccessful.future());
    }

    public Future<JsonObject> sendGET(final int messageIndex, final Expected expected) {
        log.traceEntry(() -> messageIndex);
        Promise<JsonObject> isGETSuccessful = Promise.promise();
        client.GETMessage(memory.getExpectedIDForNum(messageIndex), expected)
            .onSuccess(res -> {
                log.info(String.format("Successful GET of message number '%d' for id '%s'", messageIndex, memory.getExpectedIDForNum(messageIndex)));
                isGETSuccessful.complete(res);
            })
            .onFailure(throwable -> {
                ClientException exception = (ClientException) throwable;
                log.error(String.format("Unsuccessful GET of message number '%d' because: %s", messageIndex, exception.getMessage()));
                isGETSuccessful.fail(exception);
            });

        return log.traceExit(isGETSuccessful.future());
    }

    public Future<Void> sendDELETE(final int messageIndex, final Expected expected) throws ArrayIndexOutOfBoundsException {
        log.traceEntry(() -> messageIndex);
        Promise<Void> isDELETESuccessful = Promise.promise();
        try {
            checkValidMessageIndex(messageIndex);
            client.DELETEMessage(memory.getExpectedIDForNum(messageIndex), expected)
                .onSuccess(id -> {
                    log.info(String.format("Successful DELETE of message number '%d' with id '%s'", messageIndex, id));
                    memory.disassociateNumberFromID(messageIndex);
                    isDELETESuccessful.complete();
                })
                .onFailure(throwable -> {
                    ClientException exception = (ClientException) throwable;
                    log.error(String.format("Unsuccessful DELETE of message number '%d' because: %s", messageIndex, exception.getMessage()));
                    isDELETESuccessful.fail(throwable);
                });
        } catch (ArrayIndexOutOfBoundsException e) {
            ClientException exception = new ClientException(e.getMessage(), 500);
            log.error(exception);
            isDELETESuccessful.fail(exception);
        }
        return log.traceExit(isDELETESuccessful.future());
    }

    private void checkValidMessageIndex(final int messageIndex) throws ArrayIndexOutOfBoundsException {
        log.traceEntry(() -> messageIndex);
        if (!memory.hasOriginalMessageNumber(messageIndex)) {
            ArrayIndexOutOfBoundsException exception = new ArrayIndexOutOfBoundsException(String.format("No original message with index '%d'", messageIndex));
            log.error(exception);
            throw exception;
        }
        log.traceExit();
    }

    public Future<Void> runOperationChain(final String operationChainName) {
        log.traceEntry(() -> operationChainName);
        log.info(String.format("Running operation chain '%s'", operationChainName));

        List<Operation> operations = Optional.ofNullable(operationChains.get(operationChainName))
            .map(OperationChain::getOperations)
            .orElse(Collections.emptyList());

        boolean isParallel = Optional.ofNullable(operationChains.get(operationChainName))
            .map(OperationChain::getConfiguration)
            .map(Configuration::willRunInParallel)
            .orElse(false);


        if (!operations.isEmpty()) {
            if (isParallel) {
                log.info(String.format("Running operation chain '%s' in parallel", operationChainName));
                return runOperationsInParallel(operations);
            } else {
                log.info(String.format("Sequentially running operation chain '%s' ", operationChainName));
                return runOperationsSequentially(operations.listIterator());
            }
        }

        return log.traceExit(Future.succeededFuture());
    }

    private Future<Void> runOperationsSequentially(final Iterator<Operation> operations) {
        log.traceEntry(() -> operations);
        AtomicBoolean inFlight = new AtomicBoolean(false);
        Promise<Void> finishedOperations = Promise.promise();

        log.info("Running operations sequentially");
        this.vertx.setPeriodic(1000, id -> {
            log.info(String.format("Entering loop '%d' waiting for request to complete", id));
            if (!inFlight.get()) {
                if (operations.hasNext()) {
                    log.info(String.format("Loop '%d' has succeeded operation", id));
                    Operation op = operations.next();
                    inFlight.set(true);
                    runOperation(op)
                        .onSuccess(v -> {
                            collector.addSucceededMetric(op);
                            inFlight.set(false);
                        })
                        .onFailure(throwable -> {
                            collector.addFailedMetric(op, (ClientException) throwable);
                            vertx.cancelTimer(id);
                            inFlight.set(false);
                            finishedOperations.fail(throwable);
                        });
                } else {
                    log.info(String.format("Loop '%d' exiting as all operations have been completed", id));
                    vertx.cancelTimer(id);
                    finishedOperations.complete();
                }
            }
        });

        return log.traceExit(finishedOperations.future());
    }

    private Future<Void> runOperationsInParallel(final List<Operation> operations) {
        log.traceEntry(() -> operations);
        Promise<Void> allSuccessfulOperations = Promise.promise();
        AtomicInteger remainingOperations = new AtomicInteger(operations.size());
        AtomicReference<Throwable> firstException = new AtomicReference<>();
        log.info("Running operations in parallel");
        operations.forEach(op -> runOperation(op)
            .onComplete(res -> {
                if (res.succeeded()) {
                    log.info(String.format("Successful '%s' parallel operation", op.getAction().getMethod().toString()));
                    collector.addSucceededMetric(op);
                    remainingOperations.getAndDecrement();
                } else {
                    log.info(String.format("Failed '%s' parallel operation", op.getAction().getMethod().toString()));
                    collector.addFailedMetric(op, (ClientException) res.cause());
                    remainingOperations.getAndDecrement();
                    if (firstException.get() == null) {
                        firstException.set(res.cause());
                    }
                }
                if (remainingOperations.get() == 0 && firstException.get() == null) {
                    allSuccessfulOperations.complete();
                } else if (remainingOperations.get() == 0 && firstException.get() != null) {
                    allSuccessfulOperations.fail(firstException.get());
                }
            }));

        return log.traceExit(allSuccessfulOperations.future());
    }

    private Future<Void> runOperation(final Operation operation) {
        log.traceEntry(() -> operation);
        Promise<Void> complete = Promise.promise();
        switch (operation.getAction().getMethod()) {
            case POST:
                log.info(String.format("Running '%s' operation for message '%d'", RequestMethod.POST.toString(), operation.getAction().getMessageNumber()));
                return log.traceExit(sendPOST(operation.getAction().getMessageNumber(), operation.getExpected()));
            case GET:
                log.info(String.format("Running '%s' operation for message '%d'", RequestMethod.GET.toString(), operation.getAction().getMessageNumber()));
                sendGET(operation.getAction().getMessageNumber(), operation.getExpected())
                    .onSuccess(v -> complete.complete())
                    .onFailure(complete::fail);
                return log.traceExit(complete.future());
            case DELETE:
                log.info(String.format("Running '%s' operation for message '%d'", RequestMethod.DELETE.toString(), operation.getAction().getMessageNumber()));
                sendDELETE(operation.getAction().getMessageNumber(), operation.getExpected())
                    .onSuccess(v -> complete.complete())
                    .onFailure(complete::fail);
                return log.traceExit(complete.future());
            default:
                complete.complete();
        }
        return log.traceExit(complete.future());
    }

    public void close() {
        log.traceEntry();
        client.closeClient();
        log.traceExit();
    }

    private void readInOperationsFile(final String operationsFilePath) throws NullPointerException, IOException {
        log.traceEntry(() -> operationsFilePath);
        FileObjectMapper.readInFolderAndAddToMap(operationsFilePath, operationChains, OperationChain.class);
        log.traceExit();
    }

    public void setOperationChains(final Map<String, OperationChain> operationChains) {
        this.operationChains = operationChains;
    }
}
