package model;

import io.julian.client.model.Action;
import io.julian.client.model.RequestMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ActionTest extends AbstractOperationModelTest {
    @Test
    public void TestActionMapsCorrectly() throws IOException {
        JsonArray testContent = readTestFile();
        JsonObject firstMessage = testContent.getJsonObject(0).getJsonObject("action");

        Action action = firstMessage.mapTo(Action.class);
        Assert.assertEquals(RequestMethod.POST, action.getMethod());
        Assert.assertEquals(1, action.getMessageNumber());
    }

    @Test
    public void TestActionMapsFromEmptyJson() {
        Action action = new JsonObject().mapTo(Action.class);
        Assert.assertNull(action.getMethod());
        Assert.assertEquals(0, action.getMessageNumber());
    }
}
