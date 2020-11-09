package model.operation;

import io.julian.client.model.RequestMethod;
import io.julian.client.model.operation.Action;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ActionTest extends AbstractOperationModelTest {
    @Test
    public void TestActionMapsCorrectly() throws IOException {
        JsonObject testContent = readTestFile();
        JsonObject firstMessage = testContent.getJsonArray(OPERATIONS_KEY).getJsonObject(2).getJsonObject("action");

        Action action = firstMessage.mapTo(Action.class);
        Assert.assertEquals(RequestMethod.PUT, action.getMethod());
        Assert.assertEquals(1, action.getMessageNumber().intValue());
        Assert.assertEquals(2, action.getNewMessageNumber().intValue());
    }

    @Test
    public void TestActionMapsFromEmptyJson() {
        Action action = new JsonObject().mapTo(Action.class);
        Assert.assertNull(action.getMethod());
        Assert.assertNull(action.getMessageNumber());
        Assert.assertNull(action.getNewMessageNumber());
    }
}
