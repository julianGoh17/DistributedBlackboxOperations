package model.operation;

import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class AbstractOperationModelTest {
    protected static final String TEST_OPERATION_FILES_PATH = String.format("%s/src/test/resources/operations/sequential-test-example.json", System.getProperty("user.dir"));
    protected static final String OPERATIONS_KEY = "operations";

    public JsonObject readTestFile() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(TEST_OPERATION_FILES_PATH)));
        return new JsonObject(content);
    }
}
