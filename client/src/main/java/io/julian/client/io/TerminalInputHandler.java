package io.julian.client.io;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TerminalInputHandler {
    private final static Logger log = LogManager.getLogger(TerminalInputHandler.class.getName());
    private final InputReader reader;

    public TerminalInputHandler(final InputReader reader) {
        this.reader = reader;
    }

    public int getNumberFromInput() throws NumberFormatException {
        log.traceEntry();
        return log.traceExit(Integer.parseInt(reader.nextLine().trim()));
    }

    public String getStringFromInput() {
        log.traceEntry();
        return log.traceExit(reader.nextLine().trim());
    }

    public JsonObject getJsonObjectFromInput() throws DecodeException {
        log.traceEntry();
        return log.traceExit(new JsonObject(reader.nextLine()));
    }
}
