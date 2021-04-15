package io.julian.gossip.components;

import io.julian.gossip.models.MessageUpdate;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

public class StateTest {
    private final static String MESSAGE_ID = "messageId";
    private final static JsonObject MESSAGE = new JsonObject().put("test", new JsonObject().put("nested", "key"));

    @Test
    public void TestInit() {
        MessageStore messages = new MessageStore();
        State state = new State(messages, new ConcurrentLinkedQueue<>());
        Assert.assertEquals(messages, state.getMessages());
    }

    @Test
    public void TestGetMessageArray() {
        MessageStore messages = new MessageStore();
        State state = new State(messages, new ConcurrentLinkedQueue<>());
        messages.putMessage(MESSAGE_ID, MESSAGE);

        JsonArray array = state.getMessageArray();
        Assert.assertEquals(1, array.size());
        Assert.assertEquals(new MessageUpdate(MESSAGE_ID, MESSAGE).toJson().encodePrettily(), array.getJsonObject(0).encodePrettily());
    }

    @Test
    public void TestAddToDeadLetters() {
        MessageStore messages = new MessageStore();
        ConcurrentLinkedQueue<CoordinationMessage> deadLetters = new ConcurrentLinkedQueue<>();
        State state = new State(messages, deadLetters);
        state.addToDeadLetters(new CoordinationMessage(HTTPRequest.POST, new JsonObject()));

        Assert.assertEquals(1, deadLetters.size());
    }
}
