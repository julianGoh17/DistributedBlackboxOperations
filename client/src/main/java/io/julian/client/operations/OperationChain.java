package io.julian.client.operations;

import io.julian.client.model.operation.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperationChain {
    private static final Logger log = LogManager.getLogger(OperationChain.class.getName());

    private List<Operation> operations;
    private final Map<Integer, String> expectedMessages;

    public OperationChain() {
        expectedMessages = new HashMap<>();
        operations = new ArrayList<>();
    }

    public void readInOperationFiles(final File file) throws IOException {
        log.traceEntry(() -> file);
        operations = FileObjectMapper.readInOperationsFile(file);
        log.traceExit();
    }

    public void updateExpectedMapping(final int messageNumber, final String messageID) {
        log.traceEntry(() -> messageNumber, () -> messageID);
        expectedMessages.put(messageNumber, messageID);
        log.traceExit();
    }

    public String getExpectedMessageID(final int messageNumber) {
        log.traceEntry(() -> messageNumber);
        return log.traceExit(expectedMessages.get(messageNumber));
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public Map<Integer, String> getExpectedMessages() {
        return expectedMessages;
    }
}
