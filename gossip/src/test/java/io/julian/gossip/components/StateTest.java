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
    private final static String DELETED_ID = "deletedId";

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
    public void TestGetDeletedArray() {
        State state = new State(new MessageStore(), new ConcurrentLinkedQueue<>());
        state.addDeletedId(DELETED_ID);
        JsonArray array = state.getDeletedArray();
        Assert.assertEquals(1, array.size());
        Assert.assertEquals(DELETED_ID, array.getString(0));
    }

    @Test
    public void TestAddToDeadLetters() {
        MessageStore messages = new MessageStore();
        ConcurrentLinkedQueue<CoordinationMessage> deadLetters = new ConcurrentLinkedQueue<>();
        State state = new State(messages, deadLetters);
        state.addToDeadLetters(new CoordinationMessage(HTTPRequest.POST, new JsonObject()));

        Assert.assertEquals(1, deadLetters.size());
    }

    @Test
    public void TestAddMessageIfNotInDatabase() {
        MessageStore messages = new MessageStore();
        State state = new State(messages,  new ConcurrentLinkedQueue<>());
        Assert.assertEquals(0, state.getMessages().getNumberOfMessages());

        state.addMessageIfNotInDatabase(MESSAGE_ID, MESSAGE);
        Assert.assertEquals(1, state.getMessages().getNumberOfMessages());
        state.addMessageIfNotInDatabase(MESSAGE_ID, MESSAGE);
        Assert.assertEquals(1, state.getMessages().getNumberOfMessages());

        state.addDeletedId(DELETED_ID);
        state.addMessageIfNotInDatabase(DELETED_ID, MESSAGE);
        Assert.assertEquals(1, state.getMessages().getNumberOfMessages());
    }

    @Test
    public void TestDeleteMessageIfInDatabase() {
        MessageStore messages = new MessageStore();
        State state = new State(messages,  new ConcurrentLinkedQueue<>());
        state.addMessageIfNotInDatabase(MESSAGE_ID, MESSAGE);
        Assert.assertEquals(1, state.getMessages().getNumberOfMessages());

        state.deleteMessageIfInDatabase(MESSAGE_ID);
        Assert.assertEquals(0, state.getMessages().getNumberOfMessages());
        state.deleteMessageIfInDatabase(MESSAGE_ID);
        Assert.assertEquals(0, state.getMessages().getNumberOfMessages());
    }

    @Test
    public void TestAddInactivePostId() {
        State state = new State(new MessageStore(),  new ConcurrentLinkedQueue<>());
        Assert.assertEquals(0, state.getInactivePostIds().size());

        state.addInactivePostId(MESSAGE_ID);
        Assert.assertEquals(1, state.getInactivePostIds().size());
        state.addInactivePostId(MESSAGE_ID);
        Assert.assertEquals(1, state.getInactivePostIds().size());
    }

    @Test
    public void TestIsInactivePostId() {
        State state = new State(new MessageStore(),  new ConcurrentLinkedQueue<>());

        Assert.assertFalse(state.isInactivePostId(MESSAGE_ID));
        state.addInactivePostId(MESSAGE_ID);
        Assert.assertTrue(state.isInactivePostId(MESSAGE_ID));
    }

    @Test
    public void TestAddInactiveDeleteId() {
        State state = new State(new MessageStore(),  new ConcurrentLinkedQueue<>());
        Assert.assertEquals(0, state.getInactiveDeleteIds().size());

        state.addInactiveDeleteId(MESSAGE_ID);
        Assert.assertEquals(1, state.getInactiveDeleteIds().size());
        state.addInactiveDeleteId(MESSAGE_ID);
        Assert.assertEquals(1, state.getInactiveDeleteIds().size());
    }

    @Test
    public void TestIsInactiveDeleteId() {
        State state = new State(new MessageStore(),  new ConcurrentLinkedQueue<>());

        Assert.assertFalse(state.isInactiveDeleteId(MESSAGE_ID));
        state.addInactiveDeleteId(MESSAGE_ID);
        Assert.assertTrue(state.isInactiveDeleteId(MESSAGE_ID));
    }

    @Test
    public void TestAddDeletedId() {
        State state = new State(new MessageStore(),  new ConcurrentLinkedQueue<>());
        Assert.assertEquals(0, state.getInactivePostIds().size());

        state.addDeletedId(MESSAGE_ID);
        Assert.assertEquals(1, state.getDeletedIds().size());
        state.addDeletedId(MESSAGE_ID);
        Assert.assertEquals(1, state.getDeletedIds().size());
    }

    @Test
    public void TestIsDeletedId() {
        State state = new State(new MessageStore(),  new ConcurrentLinkedQueue<>());

        Assert.assertFalse(state.isDeletedId(MESSAGE_ID));
        state.addDeletedId(MESSAGE_ID);
        Assert.assertTrue(state.isDeletedId(MESSAGE_ID));
    }
}
