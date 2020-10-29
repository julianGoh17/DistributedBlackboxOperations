package model;

import io.vertx.core.json.JsonArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class AbstractOperationModelTest {
    private static final String TEST_OPERATION_FILES_PATH = String.format("%s/src/test/resources/operations/test-example.json", System.getProperty("user.dir"));

    public JsonArray readTestFile() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(TEST_OPERATION_FILES_PATH)));
        return new JsonArray(content);
    }
}
