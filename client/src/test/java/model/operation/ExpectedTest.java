package model.operation;

import io.julian.client.model.operation.Expected;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ExpectedTest extends AbstractOperationModelTest {
    @Test
    public void TestExpectedMapsCorrectly() throws IOException {
        JsonObject testContent = readTestFile();
        JsonObject firstMessage = testContent.getJsonArray(OPERATIONS_KEY).getJsonObject(0).getJsonObject("expected");

        Expected expected = firstMessage.mapTo(Expected.class);
        Assert.assertEquals(200, expected.getStatusCode());
        Assert.assertNull(expected.getMessageNumber());
    }

    @Test
    public void TestExpectedMapsFromEmptyJson() {
        Expected action = new JsonObject().mapTo(Expected.class);
        Assert.assertNull(action.getMessageNumber());
        Assert.assertEquals(0, action.getStatusCode());
    }
}
