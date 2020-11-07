package io.julian.client.model;

import io.julian.client.exception.ClientException;
import io.julian.client.model.operation.Operation;
import lombok.Getter;

@Getter
public class MismatchedResponse {
    private final RequestMethod method;
    private final String error;
    private final int messageNumber;
    private final int expectedStatusCode;
    private final int actualStatusCode;

    public MismatchedResponse(final Operation operation, final ClientException exception) {
        this.method = operation.getAction().getMethod();
        this.error = exception.getMessage();
        this.messageNumber = operation.getAction().getMessageNumber();
        this.expectedStatusCode = operation.getExpected().getStatusCode();
        this.actualStatusCode = exception.getStatusCode();
    }
}
