package io.julian.client.operations;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Coordinator {
    private static final Logger log = LogManager.getLogger(Coordinator.class.getName());

    private final BaseClient client;
    private final MessageMemory memory;
    private final List<OperationChain> operationChains;

    public Coordinator(final Vertx vertx) {
        client = new BaseClient(vertx);
        memory = new MessageMemory();
        operationChains = new ArrayList<>();
    }

    public void initialize(final String messageFilePath, final String operationsFilePath) throws NullPointerException, IOException {
        log.traceEntry(() -> messageFilePath, () -> operationsFilePath);
        memory.readInMessageFiles(messageFilePath);
        readInOperationsFile(operationsFilePath);
        log.traceExit();
    }

    public Future<Void> sendPOST(final int messageIndex) {
        log.traceEntry(() -> messageIndex);
        Promise<Void> isPOSTSuccessful = Promise.promise();
        client.POSTMessage(memory.getOriginalMessage(messageIndex))
            .onSuccess(id -> {
                memory.associateNumberWithID(messageIndex, id);
                isPOSTSuccessful.complete();
            })
            .onFailure(isPOSTSuccessful::fail);

        return log.traceExit(isPOSTSuccessful.future());
    }

    public Future<JsonObject> sendGET(int messageIndex) {
        log.traceEntry(() -> messageIndex);
        Promise<JsonObject> isGETSuccessful = Promise.promise();
        client.GETMessage(memory.getExpectedIDForNum(messageIndex))
            .onSuccess(isGETSuccessful::complete)
            .onFailure(isGETSuccessful::fail);

        return log.traceExit(isGETSuccessful.future());
    }

    public Future<Void> sendPUT(final int oldMessageIndex, final int newMessageIndex) {
        log.traceEntry(() -> oldMessageIndex, () -> newMessageIndex);
        Promise<Void> isPUTSuccessful = Promise.promise();
        client.PUTMessage(memory.getExpectedIDForNum(oldMessageIndex), memory.getOriginalMessage(newMessageIndex))
            .onSuccess(v -> {
                memory.associateNumberWithID(newMessageIndex, memory.getExpectedIDForNum(0));
                memory.disassociateNumberFromID(oldMessageIndex);
                isPUTSuccessful.complete();
            })
            .onFailure(isPUTSuccessful::fail);

        return log.traceExit(isPUTSuccessful.future());
    }

    private void readInOperationsFile(final String operationsFilePath) throws NullPointerException, IOException {
        log.traceEntry(() -> operationsFilePath);
        final File folder = new File(operationsFilePath);
        try {
            for (final File file : folder.listFiles()) {
                OperationChain operationChain = new OperationChain();
                operationChain.readInOperationFiles(file);
                operationChains.add(operationChain);
            }
        } catch (NullPointerException e) {
            log.error(e);
            log.traceExit();
            throw e;
        }
        log.traceExit();
    }
    
    public void close() {
        log.traceEntry();
        client.closeClient();
        log.traceExit();
    }
}
