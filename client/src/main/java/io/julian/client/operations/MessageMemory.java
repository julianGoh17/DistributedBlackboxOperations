package io.julian.client.operations;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MessageMemory {
    private static final Logger log = LogManager.getLogger(MessageMemory.class.getName());

    private final List<JsonObject> originalMessages;
    private final Map<Integer, String> expectedMapping;

    public MessageMemory() {
        originalMessages = new ArrayList<>();
        expectedMapping = new HashMap<>();
    }

    public JsonObject getOriginalMessage(final int i) {
        log.traceEntry(() -> i);
        return log.traceExit(originalMessages.get(i));
    }

    public boolean hasOriginalMessageNumber(final int messageIndex) {
        log.traceEntry(() -> messageIndex);
        return log.traceExit(messageIndex < originalMessages.size());
    }

    public void associateNumberWithID(final int messageNumber, final String messageId) {
        log.traceEntry(() -> messageNumber, () -> messageId);
        expectedMapping.put(messageNumber, messageId);
        log.traceExit();
    }

    public void disassociateNumberFromID(final int messageNumber) {
        log.traceEntry(() -> messageNumber);
        expectedMapping.remove(messageNumber);
        log.traceExit();
    }

    public String getExpectedIDForNum(final int messageNumber) {
        log.traceEntry(() -> messageNumber);
        return log.traceExit(expectedMapping.get(messageNumber));
    }

    public void readInMessageFiles(final String messageFilePath) throws NullPointerException, IOException {
        log.traceEntry(() -> messageFilePath);
        FileObjectMapper.readInFolderAndAddToList(messageFilePath, originalMessages, JsonObject.class);
        log.traceExit();
    }
}
