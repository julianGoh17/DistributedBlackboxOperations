package io.julian.client.operations;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class CoordinatorClient {
    private static final Logger log = LogManager.getLogger(CoordinatorClient.class.getName());

    private final BaseClient client;
    private final MessageMemory memory;
    private final List<OperationChain> operationChains;

    public CoordinatorClient(final Vertx vertx) {
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

    public Future<Void> sendPOST(int messageIndex) {
        log.traceEntry(() -> messageIndex);
        Promise<Void> isPOSTSuccessful = Promise.promise();
        client.POSTMessage(memory.getOriginalMessage(messageIndex))
            .onSuccess(id -> {
                memory.putIDAndMessage(id, memory.getOriginalMessage(messageIndex));
                isPOSTSuccessful.complete();
            })
            .onFailure(isPOSTSuccessful::fail);

        return log.traceExit(isPOSTSuccessful.future());
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
