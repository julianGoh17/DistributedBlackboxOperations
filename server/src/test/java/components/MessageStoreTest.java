package components;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class MessageStoreTest {
    @Test
    public void TestMessageStoreCanHoldMultipleMessages() {
        MessageStore messageStore = new MessageStore();
        messageStore.putMessage("a", new JsonObject());
        messageStore.putMessage("b", new JsonObject());
        messageStore.putMessage("c", new JsonObject());
        messageStore.putMessage("d", new JsonObject());

        Assert.assertEquals(4, messageStore.getNumberOfMessages());
    }

    @Test
    public void TestMessageStoreUpdatesWhenPuttingMessageInSameKey() {
        MessageStore messageStore = new MessageStore();
        String key = "key";
        JsonObject oldMessage = new JsonObject().put("life", "is");
        JsonObject newMessage = new JsonObject().put("sometimes", "hard");

        messageStore.putMessage(key, oldMessage);
        Assert.assertEquals(oldMessage, messageStore.getMessage(key));
        Assert.assertEquals(1, messageStore.getNumberOfMessages());

        messageStore.putMessage(key, newMessage);
        Assert.assertEquals(newMessage, messageStore.getMessage(key));
        Assert.assertEquals(1, messageStore.getNumberOfMessages());
    }

    @Test
    public void TestMessageStoreHasSameUUID() {
        MessageStore messageStore = new MessageStore();
        String key = "key";
        JsonObject oldMessage = new JsonObject().put("life", "is");

        Assert.assertFalse(messageStore.hasUUID(key));
        messageStore.putMessage(key, oldMessage);
        Assert.assertEquals(oldMessage, messageStore.getMessage(key));
        Assert.assertEquals(1, messageStore.getNumberOfMessages());
        Assert.assertTrue(messageStore.hasUUID(key));
    }
}
