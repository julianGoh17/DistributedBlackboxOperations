package model;

import io.julian.client.model.Operation;
import io.julian.client.model.RequestMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class OperationTest extends AbstractOperationModelTest {
    @Test
    public void TestOperationMapsCorrectly() throws IOException {
        JsonArray testContent = readTestFile();
        JsonObject firstMessage = testContent.getJsonObject(0);

        Operation expected = firstMessage.mapTo(Operation.class);
        Assert.assertEquals(RequestMethod.POST, expected.getAction().getMethod());
        Assert.assertEquals(1, expected.getAction().getMessageNumber());

        Assert.assertNull(expected.getExpected().getMessageNumber());
        Assert.assertEquals(200, expected.getExpected().getStatusCode());
    }

    @Test
    public void TestActionMapsFromEmptyJson() {
        Operation action = new JsonObject().mapTo(Operation.class);
        Assert.assertNull(action.getExpected());
        Assert.assertNull(action.getAction());
    }
}
