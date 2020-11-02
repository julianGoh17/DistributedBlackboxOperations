package io.julian.client.operations;

import io.vertx.core.json.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileObjectMapper {
    private static final Logger log = LogManager.getLogger(FileObjectMapper.class.getName());

    public static <T> void readInFolderAndAddToList(final String folderPath, final List<T> messages, final Class<T> mappedClass) throws NullPointerException, IOException {
        log.traceEntry(() -> folderPath, () -> messages, () -> mappedClass);
        final File folder = new File(folderPath);
        for (final File file : folder.listFiles()) {
            mapFileAndAddToList(file, messages, mappedClass);
        }
        log.traceExit();
    }

    private static <T> void mapFileAndAddToList(final File file, final List<T> list, final Class<T> mappedClass) throws IOException {
        log.traceEntry(() -> file, () -> list, () -> mappedClass);
        list.add(readFileMapToObject(file, mappedClass));
        log.traceExit();
    }

    private static <T> T readFileMapToObject(final File file, final Class<T> mappedClass) throws IOException {
        log.traceEntry(() -> file, () -> mappedClass);
        String content = FileUtils.readFileToString(file);
        if (mappedClass == JsonObject.class) {
            return log.traceExit(mappedClass.cast(new JsonObject(content)));
        } else {
            return log.traceExit(new JsonObject(content).mapTo(mappedClass));
        }
    }
}
