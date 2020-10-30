package model.operation;

import io.julian.client.model.operation.Action;
import io.julian.client.model.RequestMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import model.AbstractOperationModelTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ActionTest extends AbstractOperationModelTest {
    @Test
    public void TestActionMapsCorrectly() throws IOException {
        JsonArray testContent = readTestFile();
        JsonObject firstMessage = testContent.getJsonObject(2).getJsonObject("action");

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
