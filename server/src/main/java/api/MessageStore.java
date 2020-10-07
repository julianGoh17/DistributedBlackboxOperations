package api;

import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class MessageStore {
    private static final Logger logger = LogManager.getLogger(MessageStore.class.getName());
    private final Map<String, JsonObject> messages;

    public MessageStore() {
        messages = new HashMap<>();
    }

    public JsonObject getMessage(String uuid) {
        logger.traceEntry(() -> uuid);
        logger.info("Getting message from Message Store");
        if (messages.containsKey(uuid)) {
            return logger.traceExit(messages.get(uuid));
        }
        logger.traceExit();
        return null;
    }

    public void putMessage(String uuid, JsonObject message) {
        logger.traceEntry(() -> uuid, () -> message);
        messages.put(uuid, message);
        logger.traceExit();
    }

    public boolean hasUUID(String uuid) {
        logger.traceEntry(() -> uuid);
        return logger.traceExit(messages.containsKey(uuid));
    }
}
