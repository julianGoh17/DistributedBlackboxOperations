package io.julian.client.operations;

import io.julian.client.model.Operation;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileObjectMapper {
    private static final Logger log = LogManager.getLogger(FileObjectMapper.class.getName());

    public static void readInMessagesFile(final String messageFilePath, final List<JsonObject> messages) throws NullPointerException, IOException {
        log.traceEntry(() -> messageFilePath, () -> messages);
        final File folder = new File(messageFilePath);
        for (final File file : folder.listFiles()) {
            mapFileAndAddToList(file, messages);
        }
        log.traceExit();
    }

    private static void mapFileAndAddToList(final File file, List<JsonObject> messages) throws IOException {
        log.traceEntry(() -> file, () -> messages);
        messages.add(readFileMapToJsonObject(file));
        log.traceExit();
    }

    private static JsonObject readFileMapToJsonObject(final File file) throws IOException {
        log.traceEntry(() -> file);
        String content = FileUtils.readFileToString(file);
        return log.traceExit(new JsonObject(content));
    }

    public static List<Operation> readInOperationsFile(final File file) throws IOException {
        log.traceEntry(() -> file);
        String content = FileUtils.readFileToString(file);
        JsonArray jsonContent = new JsonArray(content);

        List<Operation> operations = new ArrayList<>();
        for (int i = 0; i < jsonContent.size(); i++) {
            operations.add(jsonContent.getJsonObject(i).mapTo(Operation.class));
        }

        return log.traceExit(operations);
    }
}
