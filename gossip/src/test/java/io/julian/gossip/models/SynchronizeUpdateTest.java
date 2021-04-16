package io.julian.gossip.models;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;
import tools.AbstractHandlerTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SynchronizeUpdateTest extends AbstractHandlerTest {
    private final static JsonObject MESSAGE = new JsonObject().put("nested", new JsonObject().put("json", "object"));
    private final static int ITEMS = 2;

    @Test
    public void TestToJson() {
        List<MessageUpdate> messages = new ArrayList<>();
        Set<String> deletedIdsSet = new HashSet<>();
        SynchronizeUpdate update = new SynchronizeUpdate(messages, deletedIdsSet);
        for (int i = 0; i < ITEMS; i++) {
            messages.add(new MessageUpdate(UUID.randomUUID().toString(), MESSAGE));
            deletedIdsSet.add(UUID.randomUUID().toString());
        }

        JsonObject json = update.toJson();
        JsonArray messageUpdates = json.getJsonArray("messages");
        JsonArray deletedIds = json.getJsonArray("deletedIds");

        Assert.assertEquals(ITEMS, messageUpdates.size());
        for (int i = 0; i < ITEMS; i++) {
            Assert.assertEquals(messages.get(i).getMessageId(), messageUpdates.getJsonObject(i).getString(MessageUpdate.MESSAGE_ID_KEY));
            Assert.assertEquals(MESSAGE.encodePrettily(), messageUpdates.getJsonObject(i).getJsonObject(MessageUpdate.MESSAGE_KEY).encodePrettily());
        }

        Assert.assertEquals(ITEMS, deletedIds.size());
        for (int i = 0; i < ITEMS; i++) Assert.assertTrue(deletedIdsSet.contains(deletedIds.getString(i)));

    }

    @Test
    public void TestFromJson() {
        JsonArray messages = new JsonArray();
        for (int i = 0; i < ITEMS; i++) messages.add(new MessageUpdate(UUID.randomUUID().toString(), MESSAGE).toJson());
        JsonArray deletedIds = new JsonArray();
        for (int i = 0; i < ITEMS; i++) deletedIds.add(UUID.randomUUID().toString());

        JsonObject json = new JsonObject()
            .put("messages", messages)
            .put("deletedIds", deletedIds);

        SynchronizeUpdate update = SynchronizeUpdate.fromJson(json);

        Assert.assertEquals(2, update.getDeletedIds().size());
        for (int i = 0; i < ITEMS; i++) {
            Assert.assertTrue(update.getDeletedIds().contains(deletedIds.getString(i)));
        }

        Assert.assertEquals(2, update.getMessages().size());
        for (int i = 0; i < ITEMS; i++) {
            Assert.assertEquals(messages.getJsonObject(i).encodePrettily(), update.getMessages().get(i).toJson().encodePrettily());
        }
    }
}
