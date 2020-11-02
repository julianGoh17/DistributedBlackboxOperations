package io.julian.client.model.operation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class OperationChain {
    private static final Logger log = LogManager.getLogger(OperationChain.class.getName());
    private List<Operation> operations;
    private Configuration configuration;
    private final Map<Integer, String> expectedMessages;

    @JsonCreator
    public OperationChain(final @JsonProperty(value = "operations", required = true) List<Operation> operations,
                         final @JsonProperty("configuration") Configuration configuration) {
        this.operations = operations;
        this.configuration = configuration;
        this.expectedMessages = new HashMap<>();
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
