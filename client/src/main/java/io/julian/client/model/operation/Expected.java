package io.julian.client.model.operation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.julian.client.exception.ClientException;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public class Expected {
    private final Logger log = LogManager.getLogger(Expected.class.getName());
    private int statusCode;

    public static final String MISMATCHED_STATUS_CODE_ERROR_FORMAT = "Error: Received '%s' status code is not equal to expected status code '%s'\n";
    public static final String SERVER_ERROR = "Server Error: %s";
    public static final String CLIENT_ERROR = "Client Error: Error with Operation Chain";

    @JsonCreator
    public Expected(@JsonProperty("statusCode") final int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean doesNotMatchExpectedStatusCode(final int statusCode) {
        log.traceEntry(() -> statusCode);
        return log.traceExit(statusCode != this.statusCode);
    }

    public ClientException generateMismatchedException(final int receivedStatusCode, final String reason) {
        log.traceEntry(() -> receivedStatusCode, () -> reason);
        return log.traceExit(new ClientException(String.format(MISMATCHED_STATUS_CODE_ERROR_FORMAT, receivedStatusCode, this.statusCode) +
            (reason != null ? String.format(SERVER_ERROR, reason) : CLIENT_ERROR),
            receivedStatusCode));
    }
}
