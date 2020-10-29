package model;

import io.julian.client.model.Expected;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ExpectedTest extends AbstractOperationModelTest {
    @Test
    public void TestExpectedMapsCorrectly() throws IOException {
        JsonArray testContent = readTestFile();
        JsonObject firstMessage = testContent.getJsonObject(0).getJsonObject("expected");

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
