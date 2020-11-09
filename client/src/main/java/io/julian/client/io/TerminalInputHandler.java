package io.julian.client.io;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TerminalInputHandler {
    private final static Logger log = LogManager.getLogger(TerminalInputHandler.class.getName());
    private final InputReader reader;
    private final Vertx vertx;

    // Need to pass in vertx to perform blocking operations (reading from terminal)
    public TerminalInputHandler(final InputReader reader, final Vertx vertx) {
        this.reader = reader;
        this.vertx = vertx;
    }

    public Future<Integer> getNumberFromInput() throws NumberFormatException {
        log.traceEntry();
        Promise<Integer> integer = Promise.promise();
        vertx.executeBlocking(
            promise -> promise.complete(reader.nextLine().trim()),
            input -> {
                try {
                    int number = Integer.parseInt((String) input.result());
                    integer.complete(number);
                } catch (NumberFormatException e) {
                    log.error(e);
                    integer.fail(e);
                }
            });
        return log.traceExit(integer.future());
    }

    public Future<String> getStringFromInput() {
        log.traceEntry();
        Promise<String> string = Promise.promise();
        vertx.executeBlocking(
            promise -> promise.complete(reader.nextLine().trim()),
            input -> string.complete((String) input.result())
        );
        return log.traceExit(string.future());
    }

    public Future<JsonObject> getJsonObjectFromInput() throws DecodeException {
        log.traceEntry();
        Promise<JsonObject> object = Promise.promise();
        vertx.executeBlocking(
            promise -> promise.complete(reader.nextLine().trim()),
            input -> {
                try {
                    object.complete(new JsonObject((String) input.result()));
                } catch (DecodeException e) {
                    log.error(e);
                    object.fail(e);
                }
            }
        );
        return log.traceExit(object.future());
    }
}
