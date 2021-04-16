package io.julian.gossip.models;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SynchronizeUpdate {
    private final static Logger log = LogManager.getLogger(SynchronizeUpdate.class);
    private final List<MessageUpdate> messages;
    private final Set<String> deletedIds;

    public final static String MESSAGES_KEY = "messages";
    public final static String DELETED_IDS_KEYS = "deletedIds";

    public SynchronizeUpdate(final List<MessageUpdate> messages, final Set<String> deletedIds) {
        this.messages = messages;
        this.deletedIds = deletedIds;
    }

    public JsonObject toJson() {
        log.traceEntry();
        JsonArray messagesJson = new JsonArray();
        this.messages.forEach(message -> messagesJson.add(message.toJson()));
        JsonArray deletedIdsJson = new JsonArray();
        this.deletedIds.forEach(deletedIdsJson::add);
        return log.traceExit(new JsonObject()
            .put(MESSAGES_KEY, messagesJson)
            .put(DELETED_IDS_KEYS, deletedIdsJson)
        );
    }

    public static SynchronizeUpdate fromJson(final JsonObject json) {
        log.traceEntry(() -> json);

        JsonArray messages = Optional.ofNullable(json)
            .map(jsonObject -> jsonObject.getJsonArray(MESSAGES_KEY))
            .orElse(new JsonArray());

        List<MessageUpdate> messageUpdates = new ArrayList<>();
        messages.stream().iterator().forEachRemaining(message -> messageUpdates.add(MessageUpdate.fromJson((JsonObject) message)));

        JsonArray deletedIds = Optional.ofNullable(json)
            .map(jsonObject -> jsonObject.getJsonArray(DELETED_IDS_KEYS))
            .orElse(new JsonArray());

        Set<String> deletedSet = new HashSet<>();
        deletedIds.stream().forEach(id -> deletedSet.add((String) id));
        return log.traceExit(new SynchronizeUpdate(messageUpdates, deletedSet));
    }

    public List<MessageUpdate> getMessages() {
        log.traceEntry();
        return log.traceExit(messages);
    }

    public Set<String> getDeletedIds() {
        log.traceEntry();
        return log.traceExit(deletedIds);
    }
}
