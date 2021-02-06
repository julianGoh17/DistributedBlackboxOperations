package io.julian.server.models.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.julian.server.models.HTTPRequest;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

@Getter
@Setter
public class ClientMessage {
    private static final Logger log = LogManager.getLogger(ClientMessage.class.getName());

    private final HTTPRequest request;
    private final JsonObject message;
    private final String originalId;
    private final String newId;

    public static final String REQUEST_KEY = "request";
    public static final String MESSAGE_KEY = "message";
    public static final String ORIGINAL_ID_KEY = "originalId";
    public static final String NEW_ID_KEY = "newId";

    public ClientMessage(final HTTPRequest request, final JsonObject message, final String originalId, final String newId) {
        this.request = request;
        this.message = message;
        this.originalId = originalId;
        this.newId = newId;
    }

    public ClientMessage(final HTTPRequest request, final JsonObject message, final String originalId) {
        this.request = request;
        this.message = message;
        this.originalId = originalId;
        this.newId = null;
    }

    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(REQUEST_KEY, this.request.toValue())
            .put(MESSAGE_KEY, this.message)
            .put(ORIGINAL_ID_KEY, this.originalId)
            .put(NEW_ID_KEY, this.newId));
    }

    @JsonCreator
    public static ClientMessage fromJson(final JsonObject object) {
        log.traceEntry(() -> object);

        HTTPRequest request = Optional.ofNullable(object)
            .map(obj -> obj.getString(REQUEST_KEY))
            .map(HTTPRequest::forValue)
            .orElse(HTTPRequest.UNKNOWN);

        JsonObject message = Optional.ofNullable(object)
            .map(obj -> obj.getJsonObject(MESSAGE_KEY))
            .orElse(null);

        String originalId = object.getString(ORIGINAL_ID_KEY);

        String newId = object.getString(NEW_ID_KEY);

        return log.traceExit(new ClientMessage(request, message, originalId, newId));
    }
}
