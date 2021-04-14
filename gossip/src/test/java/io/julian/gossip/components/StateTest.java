package io.julian.gossip.components;

import io.julian.gossip.models.MessageUpdate;
import io.julian.server.components.MessageStore;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class StateTest {
    private final static String MESSAGE_ID = "messageId";
    private final static JsonObject MESSAGE = new JsonObject().put("test", new JsonObject().put("nested", "key"));

    @Test
    public void TestInit() {
        MessageStore messages = new MessageStore();
        State state = new State(messages);
        Assert.assertEquals(messages, state.getMessages());
    }

    @Test
    public void TestGetMessageArray() {
        MessageStore messages = new MessageStore();
        State state = new State(messages);
        messages.putMessage(MESSAGE_ID, MESSAGE);

        JsonArray array = state.getMessageArray();
        Assert.assertEquals(1, array.size());
        Assert.assertEquals(new MessageUpdate(MESSAGE_ID, MESSAGE).toJson().encodePrettily(), array.getJsonObject(0).encodePrettily());
    }
}
