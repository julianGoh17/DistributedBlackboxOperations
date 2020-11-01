package io.julian.client.Exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientException extends Throwable {
    private static final Logger log = LogManager.getLogger(ClientException.class);
    private final int statusCode;

    public ClientException(final String error, final int statusCode) {
        super(error);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        log.traceEntry();
        return log.traceExit(statusCode);
    }
}
