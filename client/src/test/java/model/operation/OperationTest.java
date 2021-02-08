package model.operation;

import io.julian.client.model.RequestMethod;
import io.julian.client.model.operation.Operation;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class OperationTest extends AbstractOperationModelTest {
    @Test
    public void TestOperationMapsCorrectly() throws IOException {
        JsonObject testContent = readTestFile();
        JsonObject firstMessage = testContent.getJsonArray(OPERATIONS_KEY).getJsonObject(0);

        Operation expected = firstMessage.mapTo(Operation.class);
        Assert.assertEquals(RequestMethod.POST, expected.getAction().getMethod());
        Assert.assertEquals(1, expected.getAction().getMessageNumber().intValue());

        Assert.assertEquals(200, expected.getExpected().getStatusCode());
    }

    @Test
    public void TestActionMapsFromEmptyJson() {
        Operation action = new JsonObject().mapTo(Operation.class);
        Assert.assertNull(action.getExpected());
        Assert.assertNull(action.getAction());
    }
}
