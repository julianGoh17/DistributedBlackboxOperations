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
    private final Map<String, JsonObject> expectedMapping;

    public MessageMemory() {
        originalMessages = new ArrayList<>();
        expectedMapping = new HashMap<>();
    }

    public JsonObject getOriginalMessage(final int i) {
        log.traceEntry(() -> i);
        return log.traceExit(originalMessages.get(i));
    }

    public void putIDAndMessage(final String id, final JsonObject message) {
        log.traceEntry(() -> id, () -> message);
        expectedMapping.put(id, message);
        log.traceExit();
    }

    public JsonObject getExpectedMessageForID(final String id) {
        log.traceEntry(() -> id);
        return log.traceExit(expectedMapping.get(id));
    }

    public void readInMessageFiles(final String messageFilePath) throws NullPointerException, IOException {
        log.traceEntry(() -> messageFilePath);
        FileObjectMapper.readInMessagesFile(messageFilePath, originalMessages);
        log.traceExit();
    }
}
