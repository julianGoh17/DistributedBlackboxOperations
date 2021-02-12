package io.julian.client.operations;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MessageMemory {
    private static final Logger log = LogManager.getLogger(MessageMemory.class.getName());

    // TODO: Use Map instead of list?
    private List<JsonObject> originalMessages;
    private Map<Integer, String> expectedMapping;

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
        log.info(String.format("Associating message number '%d' with id '%s'", messageNumber, messageId));
        expectedMapping.put(messageNumber, messageId);
        log.traceExit();
    }

    public void disassociateNumberFromID(final int messageNumber) {
        log.traceEntry(() -> messageNumber);
        log.info(String.format("Removing associated id for message number '%d'", messageNumber));
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
